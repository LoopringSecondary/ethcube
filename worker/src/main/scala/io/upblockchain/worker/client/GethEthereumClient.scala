package io.upblockchain.worker.client

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream._
import akka.stream.scaladsl._
import io.upblockchain.worker.modules.GethClientConfig
import javax.inject.Inject

class GethEthereumClient @Inject() (system: ActorSystem, materilizer: ActorMaterializer, eth: GethClientConfig) {

  import system.dispatcher

  val poolClientFlow: Flow[(HttpRequest, Promise[HttpResponse]), (Try[HttpResponse], Promise[HttpResponse]), Http.HostConnectionPool] = {
    eth.ssl match {
      case true ⇒ Http()(system).cachedHostConnectionPoolHttps[Promise[HttpResponse]](host = eth.host, port = eth.port)
      case _ ⇒ Http()(system).cachedHostConnectionPool[Promise[HttpResponse]](host = eth.host, port = eth.port)
    }
  }

  // TODO(Toan) 下面的东西还需要优化一下
  val queue: SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])] =
    Source.queue[(HttpRequest, Promise[HttpResponse])](100, OverflowStrategy.backpressure)
      .via(poolClientFlow)
      .toMat(Sink.foreach({
        case (Success(resp), p) ⇒ p.success(resp)
        case (Failure(e), p) ⇒ p.failure(e) // 这里可以定制一个json
      }))(Keep.left).run()(materilizer)

  def handleRequest(request: HttpRequest): Future[HttpResponse] = {
    val responsePromise = Promise[HttpResponse]()
    queue.offer(request -> responsePromise).flatMap {
      case QueueOfferResult.Enqueued ⇒ responsePromise.future
      case QueueOfferResult.Dropped ⇒ Future.failed(new RuntimeException("Queue overflowed. Try again later."))
      case QueueOfferResult.Failure(ex) ⇒ Future.failed(ex)
      case QueueOfferResult.QueueClosed ⇒ Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
    }
  }

}