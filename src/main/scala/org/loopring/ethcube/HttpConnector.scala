/*
 * Copyright 2018 Loopring Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.loopring.ethcube

import scala.util._
import scala.concurrent._
import akka.actor._
import akka.stream._
import akka.http.scaladsl.model._
import akka.http.scaladsl._
import akka.stream.scaladsl._
import akka.pattern.pipe
import akka.http.scaladsl.marshalling.Marshal
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s._
import org.loopring.ethcube.proto.data._
import scalapb.json4s.JsonFormat
import org.loopring.ethcube.proto.eth_jsonrpc._

private[ethcube] class HttpConnector(node: EthereumProxySettings.Node)(
    implicit
    val materilizer: ActorMaterializer
) extends Actor
  with ActorLogging
  with Json4sSupport {

  import context.dispatcher

  implicit val serialization = jackson.Serialization //.formats(NoTypeHints)
  implicit val system: ActorSystem = context.system
  implicit val formats = org.json4s.native.Serialization.formats(NoTypeHints) + new EmptyValueSerializer

  val DEBUG_TIMEOUT_STR = "5s"
  val DEBUG_TRACER = "callTracer"
  val ETH_CALL = "eth_call"
  val RETRY_MAX = 5

  private val poolClientFlow: Flow[(HttpRequest, Promise[HttpResponse]), (Try[HttpResponse], Promise[HttpResponse]), Http.HostConnectionPool] = {
    Http().cachedHostConnectionPool[Promise[HttpResponse]](
      host = node.host,
      port = node.port
    )
  }

  log.info(s"connecting Ethereum at ${node.host}:${node.port}")

  private val queue: SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])] =
    Source
      .queue[(HttpRequest, Promise[HttpResponse])](
        100,
        OverflowStrategy.backpressure
      )
      .via(poolClientFlow)
      .toMat(Sink.foreach({
        case (Success(resp), p) ⇒ p.success(resp)
        case (Failure(e), p)    ⇒ p.failure(e)
      }))(Keep.left)
      .run()(materilizer)

  private def request(request: HttpRequest): Future[HttpResponse] = {
    val responsePromise = Promise[HttpResponse]()
    queue.offer(request -> responsePromise).flatMap {
      case QueueOfferResult.Enqueued ⇒
        responsePromise.future
      case QueueOfferResult.Dropped ⇒
        Future.failed(new RuntimeException("Queue overflowed."))
      case QueueOfferResult.Failure(ex) ⇒
        Future.failed(ex)
      case QueueOfferResult.QueueClosed ⇒
        Future.failed(new RuntimeException("Queue closed."))
    }
  }

  private def post(json: String): Future[String] = {
    post(HttpEntity(ContentTypes.`application/json`, json))
  }

  private def post(entity: RequestEntity): Future[String] = {
    for {
      httpResp ← request(
        HttpRequest(method = HttpMethods.POST, entity = entity)
      )
      jsonStr ← httpResp.entity.dataBytes.map(_.utf8String).runReduce(_ + _)
    } yield jsonStr
  }

  private def sendMessage[T <: ProtoBuf[T]](method: String)(params: Seq[Any])(
    implicit
    c: scalapb.GeneratedMessageCompanion[T]
  ): Future[T] = {
    val jsonRpc = JsonRpcReqWrapped(
      id = Random.nextInt(100),
      jsonrpc = "2.0",
      method = method,
      params = params
    )
    log.info(s"reqeust: ${org.json4s.native.Serialization.write(jsonRpc)}")
    val resp = for {
      entity ← Marshal(jsonRpc).to[RequestEntity]
      jsonStr ← post(entity)
      _ = log.info(s"response: $jsonStr")
    } yield JsonFormat.fromJsonString[T](jsonStr)
    resp pipeTo sender
  }

  private def retryEthBlockNumber(num: Int): Future[EthBlockNumberRes] = for {
    res ← sendMessage[EthBlockNumberRes]("eth_blockNumber") { Seq.empty }
    r ← if (res.error.nonEmpty) {
      throw new Exception(res.error.toString)
    } else if (num - 1 > 0 && hex2BigInt(res.result).compare(BigInt(1)) <= 0) {
      retryEthBlockNumber(num - 1)
    } else {
      Future.successful(res)
    }
  } yield r

  private def hex2BigInt(s: String) = BigInt(s.replace("0x", ""), 16)

  def receive: Receive = {
    case req: JsonRpcReq      ⇒ post(req.json).map(JsonRpcRes(_)) pipeTo sender
    case r: EthBlockNumberReq ⇒ retryEthBlockNumber(RETRY_MAX)
    case r: EthGetBalanceReq ⇒
      sendMessage[EthGetBalanceRes]("eth_getBalance") {
        Seq(r.address, r.tag)
      }
    case r: GetTransactionByHashReq ⇒
      sendMessage[GetTransactionByHashRes]("eth_getTransactionByHash") {
        Seq(r.hash)
      }
    case r: GetTransactionReceiptReq ⇒
      sendMessage[GetTransactionReceiptRes]("eth_getTransactionReceipt") {
        Seq(r.hash)
      }
    case r: GetBlockWithTxHashByNumberReq ⇒
      sendMessage[GetBlockWithTxHashByNumberRes]("eth_getBlockByNumber") {
        Seq(r.blockNumber, false)
      }
    case r: GetBlockWithTxObjectByNumberReq ⇒
      sendMessage[GetBlockWithTxObjectByNumberRes]("eth_getBlockByNumber") {
        Seq(r.blockNumber, true)
      }
    case r: GetBlockWithTxHashByHashReq ⇒
      sendMessage[GetBlockWithTxHashByHashRes]("eth_getBlockByHash") {
        Seq(r.blockHash, false)
      }
    case r: GetBlockWithTxObjectByHashReq ⇒
      sendMessage[GetBlockWithTxObjectByHashRes]("eth_getBlockByHash") {
        Seq(r.blockHash, true)
      }
    case r: TraceTransactionReq ⇒
      sendMessage[TraceTransactionRes]("debug_traceTransaction") {
        val debugParams = DebugParams(DEBUG_TIMEOUT_STR, DEBUG_TRACER)
        Seq(r.txhash, debugParams)
      }
    case r: SendRawTransactionReq ⇒
      sendMessage[SendRawTransactionRes]("eth_sendRawTransaction") {
        Seq(r.data)
      }
    case r: GetEstimatedGasReq ⇒
      sendMessage[GetEstimatedGasRes]("eth_estimateGas") {
        val args = TransactionParam().withTo(r.to).withData(r.data)
        Seq(args)
      }
    case r: GetNonceReq ⇒
      sendMessage[GetNonceRes]("eth_getTransactionCount") {
        Seq(r.owner, r.tag)
      }
    case r: GetBlockTransactionCountReq ⇒
      sendMessage[GetBlockTransactionCountRes](
        "eth_getBlockTransactionCountByHash"
      ) {
          Seq(r.blockHash)
        }
    case r: EthCallReq ⇒
      sendMessage[EthCallRes]("eth_call") {
        Seq(r.param, r.tag)
      }
  }

}

private case class DebugParams(timeout: String, tracer: String)

private class EmptyValueSerializer
  extends CustomSerializer[String](
    _ ⇒
      ({
        case JNull ⇒ ""
      }, {
        case "" ⇒ JNothing
      })
  )
