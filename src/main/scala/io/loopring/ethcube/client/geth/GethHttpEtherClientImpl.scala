package io.loopring.ethcube.client.geth

import io.loopring.ethcube.client.EtherClient
import akka.actor.Actor
import scala.concurrent.Future
import javax.inject.Inject
import io.loopring.ethcube.modules.EtherClientConfig
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.slf4j.LoggerFactory
import akka.stream.scaladsl.Flow
import scala.util.Try
import scala.concurrent.Promise
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpResponse
import akka.stream.scaladsl.SourceQueueWithComplete
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink
import scala.util.Success
import scala.util.Failure
import akka.stream.scaladsl.Keep
import akka.stream.QueueOfferResult
import io.loopring.ethcube.model.JsonRpcRequest
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.ContentTypes
import akka.util.ByteString
import akka.http.scaladsl.model.HttpEntity
import akka.pattern.pipe
import io.loopring.ethcube.model.BroadcastResponse

class GethHttpEtherClientImpl(sys: ActorSystem, mat: ActorMaterializer, val config: EtherClientConfig) extends EtherClient with Actor {

  lazy val Log = LoggerFactory.getLogger(getClass)
  implicit val materilizer = mat

  import sys.dispatcher

  private val poolClientFlow: Flow[(HttpRequest, Promise[HttpResponse]), (Try[HttpResponse], Promise[HttpResponse]), Http.HostConnectionPool] = {
    Http()(sys).cachedHostConnectionPool[Promise[HttpResponse]](host = config.host, port = config.port)
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
      case QueueOfferResult.Enqueued ⇒
        responsePromise.future
      case QueueOfferResult.Dropped ⇒
        Future.failed(new RuntimeException("Queue overflowed. Try again later."))
      case QueueOfferResult.Failure(ex) ⇒
        Future.failed(ex)
      case QueueOfferResult.QueueClosed ⇒
        Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
    }
  }

  def receive: Actor.Receive = {
    case req: JsonRpcRequest ⇒
      //      val httpReq = HttpRequest(
      //        method = HttpMethods.POST,
      //        // TODO(Toan) 这里的json需要处理
      //        entity = HttpEntity(ContentTypes.`application/json`, ByteString("")))
      //      val resp = for {
      //        httpResp ← request(httpReq)
      //        jsonResp ← httpResp.entity.dataBytes.map(_.utf8String).runReduce(_ + _)
      //        _ = Log.info(s"geth http client json response => ${jsonResp}")
      //      } yield jsonResp // JsonRPCResponse(id = req.id, json = jsonResp)

      Future.successful(BroadcastResponse(label = context.self.path.address.toString)) pipeTo sender

  }

}