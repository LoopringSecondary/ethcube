package io.upblockchain.worker.client.geth

import io.upblockchain.worker.client.GethEthereumClient
import io.upblockchain.proto.jsonrpc._
import scala.concurrent._
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.model._
import scala.util._
import akka.http.scaladsl.Http
import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.upblockchain.worker.modules.GethClientConfig
import akka.stream.scaladsl._
import akka.stream._
import akka.util.ByteString
import org.slf4j.LoggerFactory
import io.upblockchain.worker.client.GethEthereumClient

class GethHttpClientImpl @Inject() (system: ActorSystem, materilizer: ActorMaterializer, val eth: GethClientConfig) extends GethEthereumClient {

  lazy val Log = LoggerFactory.getLogger(getClass)
  implicit val mat = materilizer
  import system.dispatcher

  private val poolClientFlow: Flow[(HttpRequest, Promise[HttpResponse]), (Try[HttpResponse], Promise[HttpResponse]), Http.HostConnectionPool] = {
    eth.ssl match {
      case true ⇒ Http()(system).cachedHostConnectionPoolHttps[Promise[HttpResponse]](host = eth.host, port = eth.port)
      case _ ⇒ Http()(system).cachedHostConnectionPool[Promise[HttpResponse]](host = eth.host, port = eth.port)
    }
  }

  // TODO(Toan) 下面的东西还需要优化一下
  private val queue: SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])] =
    Source.queue[(HttpRequest, Promise[HttpResponse])](100, OverflowStrategy.backpressure)
      .via(poolClientFlow)
      .toMat(Sink.foreach({
        case (Success(resp), p) ⇒ p.success(resp)
        case (Failure(e), p) ⇒ p.failure(e) // 这里可以定制一个json
      }))(Keep.left).run()(materilizer)

  private def request(request: HttpRequest): Future[HttpResponse] = {
    val responsePromise = Promise[HttpResponse]()
    queue.offer(request -> responsePromise).flatMap {
      case QueueOfferResult.Enqueued ⇒ responsePromise.future
      case QueueOfferResult.Dropped ⇒ Future.failed(new RuntimeException("Queue overflowed. Try again later."))
      case QueueOfferResult.Failure(ex) ⇒ Future.failed(ex)
      case QueueOfferResult.QueueClosed ⇒ Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
    }
  }

  def handleRequest(req: JsonRPCRequest): Future[JsonRPCResponse] = {
    val httpReq = HttpRequest(
      method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, ByteString(req.json)))
    for {
      httpResp ← request(httpReq)
      jsonResp ← httpResp.entity.dataBytes.map(_.utf8String).runReduce(_ + _)
      _ = Log.info(s"geth http client json response => ${jsonResp}")
    } yield JsonRPCResponse(id = req.id, json = jsonResp)
  }

}