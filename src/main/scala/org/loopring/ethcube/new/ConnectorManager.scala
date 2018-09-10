package org.loopring.ethcube

import akka.actor._
import akka.routing._
import org.loopring.ethcube.proto.data._

private class ConnectionManager(connectorRouter: Router)
  extends Actor with ActorLogging {

  def receive: Receive = {
    case _ =>
  }
}