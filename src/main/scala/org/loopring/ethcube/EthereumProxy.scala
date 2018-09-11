package org.loopring.ethcube

import akka.actor._
import akka.routing._
import akka.util.Timeout
import akka.stream.ActorMaterializer
import org.loopring.ethcube.proto.data._
import scala.collection.immutable.IndexedSeq

class EthereumProxy(settings: EthereumProxySettings)(
  implicit
  materilizer: ActorMaterializer)
  extends Actor with ActorLogging {

  private val connectorGroups: Seq[ActorRef] = settings.nodes.map {
    node =>
      val props =
        if (node.ipcPath.nonEmpty) Props(new IpcConnector(node))
        else Props(new HttpConnector(node))

      context.actorOf(
        RoundRobinPool(settings.poolSize).props(props),
        "connector_group")
  }

  private val topRouter = new Router(
    RoundRobinRoutingLogic(),
    IndexedSeq(connectorGroups.map(ActorRefRoutee): _*))

  private val manager = context.actorOf(
    Props(new ConnectionManager(
      topRouter,
      connectorGroups,
      settings.checkIntervalSeconds,
      settings.healthyThreshold)),
    "ethereum_connector_manager")

  def receive: Receive = {
    case m: JsonRpcReq =>
      if (topRouter.routees.isEmpty) {
        sender ! JsonRpcRes(
          id = None,
          jsonrpc = "2.0",
          error = Some(JsonRpcErr(600, "has no routee")))
      } else {
        topRouter.route(m, sender)
      }
  }
}