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
import org.json4s.native.JsonMethods.parse
import org.loopring.ethcube.proto.data._
import org.loopring.ethcube.proto.eth_jsonrpc._
import scalapb.json4s.JsonFormat

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

  val emptyError = EthResError(code = 500, error = "result is empty")

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

  private def sendMessage(method: String)(params: Seq[Any]): Future[String] = {
    val jsonRpc = JsonRpcReqWrapped(
      id = Random.nextInt(100),
      jsonrpc = "2.0",
      method = method,
      params = params
    )
    log.info(s"reqeust: ${org.json4s.native.Serialization.write(jsonRpc)}")

    for {
      entity ← Marshal(jsonRpc).to[RequestEntity]
      jsonStr ← post(entity)
      _ = log.info(s"response: $jsonStr")
    } yield jsonStr

  }

  private def toResponseWrapped: PartialFunction[String, JsonRpcResWrapped] = {
    case json: String ⇒ parse(json).extract[JsonRpcResWrapped]
  }

  private def checkResponseWrapped: PartialFunction[JsonRpcResWrapped, Boolean] = {
    case res: JsonRpcResWrapped ⇒ res.result.toString.isEmpty
  }

  private def hex2BigInt(s: String) = BigInt(s.replace("0x", ""), 16)

  def receive: Receive = {
    case req: JsonRpcReq ⇒ post(req.json).map(JsonRpcRes(_)) pipeTo sender
    case r: EthBlockNumberReq ⇒
      sendMessage("eth_blockNumber") {
        Seq.empty
      } map { json ⇒
        (checkResponseWrapped compose toResponseWrapped)(json) match {
          case true ⇒
            EthBlockNumberRes().withJsonrpc("2.0")
              .withError(emptyError)
          case _ ⇒ JsonFormat.fromJsonString[EthBlockNumberRes](json)
        }
      } pipeTo sender

    case r: EthGetBalanceReq ⇒
      sendMessage("eth_getBalance") {
        Seq(r.address, r.tag)
      } map { json ⇒
        (checkResponseWrapped compose toResponseWrapped)(json) match {
          case true ⇒
            EthGetBalanceRes().withJsonrpc("2.0")
              .withError(emptyError)
          case _ ⇒ JsonFormat.fromJsonString[EthGetBalanceRes](json)

        }
      } pipeTo sender

    case r: GetTransactionByHashReq ⇒
      sendMessage("eth_getTransactionByHash") {
        Seq(r.hash)
      } map { json ⇒
        (checkResponseWrapped compose toResponseWrapped)(json) match {
          case true ⇒
            GetTransactionByHashRes().withJsonrpc("2.0")
              .withError(emptyError)
          case _ ⇒ JsonFormat.fromJsonString[GetTransactionByHashRes](json)
        }
      } pipeTo sender

    case r: GetTransactionReceiptReq ⇒
      sendMessage("eth_getTransactionReceipt") {
        Seq(r.hash)
      } map { json ⇒
        (checkResponseWrapped compose toResponseWrapped)(json) match {
          case true ⇒
            GetTransactionReceiptRes().withJsonrpc("2.0")
              .withError(emptyError)
          case _ ⇒ JsonFormat.fromJsonString[GetTransactionReceiptRes](json)
        }
      } pipeTo sender
    case r: GetBlockWithTxHashByNumberReq ⇒
      sendMessage("eth_getBlockByNumber") {
        Seq(r.blockNumber, false)
      } map { json ⇒
        (checkResponseWrapped compose toResponseWrapped)(json) match {
          case true ⇒
            GetBlockWithTxHashByNumberRes().withJsonrpc("2.0")
              .withError(emptyError)
          case _ ⇒ JsonFormat.fromJsonString[GetBlockWithTxHashByNumberRes](json)
        }
      } pipeTo sender
    case r: GetBlockWithTxObjectByNumberReq ⇒
      sendMessage("eth_getBlockByNumber") {
        Seq(r.blockNumber, true)
      } map { json ⇒
        (checkResponseWrapped compose toResponseWrapped)(json) match {
          case true ⇒
            GetBlockWithTxObjectByNumberRes().withJsonrpc("2.0")
              .withError(emptyError)
          case _ ⇒ JsonFormat.fromJsonString[GetBlockWithTxObjectByNumberRes](json)

        }
      } pipeTo sender
    case r: GetBlockWithTxHashByHashReq ⇒
      sendMessage("eth_getBlockByHash") {
        Seq(r.blockHash, false)
      } map { json ⇒
        (checkResponseWrapped compose toResponseWrapped)(json) match {
          case true ⇒
            GetBlockWithTxHashByHashRes().withJsonrpc("2.0")
              .withError(emptyError)
          case _ ⇒ JsonFormat.fromJsonString[GetBlockWithTxHashByHashRes](json)
        }
      } pipeTo sender
    case r: GetBlockWithTxObjectByHashReq ⇒
      sendMessage("eth_getBlockByHash") {
        Seq(r.blockHash, true)
      } map { json ⇒
        (checkResponseWrapped compose toResponseWrapped)(json) match {
          case true ⇒
            GetBlockWithTxObjectByHashRes().withJsonrpc("2.0")
              .withError(emptyError)
          case _ ⇒ JsonFormat.fromJsonString[GetBlockWithTxObjectByHashRes](json)
        }
      } pipeTo sender
    case r: TraceTransactionReq ⇒
      sendMessage("debug_traceTransaction") {
        val debugParams = DebugParams(DEBUG_TIMEOUT_STR, DEBUG_TRACER)
        Seq(r.txhash, debugParams)
      } map { json ⇒
        (checkResponseWrapped compose toResponseWrapped)(json) match {
          case true ⇒
            TraceTransactionRes().withJsonrpc("2.0")
              .withError(emptyError)
          case _ ⇒ JsonFormat.fromJsonString[TraceTransactionRes](json)
        }
      } pipeTo sender
    case r: SendRawTransactionReq ⇒
      sendMessage("eth_sendRawTransaction") {
        Seq(r.data)
      } map { json ⇒
        (checkResponseWrapped compose toResponseWrapped)(json) match {
          case true ⇒
            SendRawTransactionRes().withJsonrpc("2.0")
              .withError(emptyError)
          case _ ⇒ JsonFormat.fromJsonString[SendRawTransactionRes](json)
        }
      } pipeTo sender
    case r: GetEstimatedGasReq ⇒
      sendMessage("eth_estimateGas") {
        val args = TransactionParam().withTo(r.to).withData(r.data)
        Seq(args)
      } map { json ⇒
        (checkResponseWrapped compose toResponseWrapped)(json) match {
          case true ⇒
            GetEstimatedGasRes().withJsonrpc("2.0")
              .withError(emptyError)
          case _ ⇒ JsonFormat.fromJsonString[GetEstimatedGasRes](json)
        }
      } pipeTo sender
    case r: GetNonceReq ⇒
      sendMessage("eth_getTransactionCount") {
        Seq(r.owner, r.tag)
      } map { json ⇒
        (checkResponseWrapped compose toResponseWrapped)(json) match {
          case true ⇒
            GetNonceRes().withJsonrpc("2.0")
              .withError(emptyError)
          case _ ⇒ JsonFormat.fromJsonString[GetNonceRes](json)
        }
      } pipeTo sender
    case r: GetBlockTransactionCountReq ⇒
      sendMessage("eth_getBlockTransactionCountByHash") {
        Seq(r.blockHash)
      } map { json ⇒
        (checkResponseWrapped compose toResponseWrapped)(json) match {
          case true ⇒
            GetBlockTransactionCountRes().withJsonrpc("2.0")
              .withError(emptyError)
          case _ ⇒ JsonFormat.fromJsonString[GetBlockTransactionCountRes](json)
        }
      } pipeTo sender
    case r: EthCallReq ⇒
      sendMessage("eth_call") {
        Seq(r.param, r.tag)
      } map { json ⇒
        (checkResponseWrapped compose toResponseWrapped)(json) match {
          case true ⇒
            EthCallRes().withJsonrpc("2.0")
              .withError(emptyError)
          case _ ⇒ JsonFormat.fromJsonString[EthCallRes](json)
        }
      } pipeTo sender
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
