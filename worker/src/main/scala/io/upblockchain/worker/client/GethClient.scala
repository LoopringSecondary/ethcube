package io.upblockchain.worker.client

import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.model.HttpRequest
import scala.concurrent.Promise
import scala.util.Try
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.Http
import akka.stream.scaladsl.SourceQueueWithComplete
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Source
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import akka.stream.QueueOfferResult

class GethClient @Inject() (system: ActorSystem, materilizer: ActorMaterializer) {

  import system.dispatcher

  val poolClientFlow: Flow[(HttpRequest, Promise[HttpResponse]), (Try[HttpResponse], Promise[HttpResponse]), Http.HostConnectionPool] = {
    //    Http()(system).cachedHostConnectionPool(host, port, settings, log)
    Http()(system).cachedHostConnectionPool[Promise[HttpResponse]](host = "192.168.0.200", port = 8545)
  }
  // Http()(system).cachedHostConnectionPoolHttps[Promise[HttpResponse]]("192.168.0.200:8545").

  val queue: SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])] =
    Source.queue[(HttpRequest, Promise[HttpResponse])](100, OverflowStrategy.backpressure)
      .via(poolClientFlow)
      .toMat(Sink.foreach({
        case (Success(resp), p) ⇒ p.success(resp)
        case (Failure(e), p) ⇒ p.failure(e)
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