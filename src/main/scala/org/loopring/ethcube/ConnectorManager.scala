package org.loopring.ethcube

import scala.concurrent._
import akka.pattern.ask
import akka.actor._
import akka.util.Timeout
import akka.routing._
import org.loopring.ethcube.proto.data._
import scala.concurrent.duration._

private class ConnectionManager(
  topRouter: Router,
  connectorGroups: Seq[ActorRef],
  checkIntervalSeconds: Int,
  healthyThreshold: Float)
  extends Actor with ActorLogging {

  implicit val ec = context.system.dispatcher
  implicit val timeout: Timeout = Timeout(1 seconds)
  private val size = connectorGroups.size

  context.system.scheduler.schedule(
    checkIntervalSeconds.seconds,
    checkIntervalSeconds.seconds,
    self,
    CheckBlockHeight)

  def receive: Receive = {

    case m: CheckBlockHeight =>
      for {
        resps: Seq[(ActorRef, Int)] <- Future.sequence(connectorGroups.map {
          g =>
            for {
              resp <- (g ? m).mapTo[CheckBlockHeightResp].recover {
                case _: TimeoutException => CheckBlockHeightResp(0)
                case _: Throwable => CheckBlockHeightResp(-1)
              }
            } yield (g, resp.blockHeight)
        })
        highestBlock: Int = resps.map(_._2).reduce(Math.max(_, _))
        tier1: Seq[ActorRef] = resps.filter(highestBlock - _._2 < 1).map(_._1)
        tier2: Seq[ActorRef] = resps.filter(highestBlock - _._2 < 2).map(_._1)
        tier3: Seq[ActorRef] = resps.filter(highestBlock - _._2 < 3).map(_._1)

        goodGroups = if (tier1.size >= size * healthyThreshold) tier1
        else if (tier2.size >= size * healthyThreshold) tier2
        else tier3

        badGroups = connectorGroups.filter(goodGroups.contains)
      } yield {
        goodGroups.foreach { g => topRouter.addRoutee(ActorRefRoutee(g)) }
        badGroups.foreach { g => topRouter.removeRoutee(ActorRefRoutee(g)) }

        log.debug(s"${goodGroups.size} connectorGroup are still in good shape "
          + s"(blockheight: $highestBlock): ${resps.mkString("[", ", ", "]")}")
      }
  }
}