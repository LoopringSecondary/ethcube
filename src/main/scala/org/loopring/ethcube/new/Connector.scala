package org.loopring.ethcube

import scala.util.Try
import scala.concurrent.Promise
import akka.actor._
import akka.routing._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import org.loopring.ethcube.proto.data._

private class Connector(node: EthereumProxySettings.Node)(implicit val mat: ActorMaterializer)
  extends Actor with ActorLogging {

  import context.dispatcher
  implicit val system: ActorSystem = context.system

  // private val poolClientFlow: Flow[(HttpRequest, Promise[HttpResponse]), (Try[HttpResponse], Promise[HttpResponse]), Http.HostConnectionPool] = {
  //   Http.cachedHostConnectionPool[Promise[HttpResponse]](host = node.host, port = node.port)
  // }

  // check_interval_seconds
  def receive: Receive = {
    case _ =>
  }
}