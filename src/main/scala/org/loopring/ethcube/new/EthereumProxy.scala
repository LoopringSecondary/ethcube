package org.loopring.ethcube

import akka.actor._
import akka.routing._
import akka.stream.ActorMaterializer
import org.loopring.ethcube.proto.data._
import scala.collection.immutable.IndexedSeq

class EthereumProxy(settings: EthereumProxySettings)(implicit val mat: ActorMaterializer)
  extends Actor with ActorLogging {

  private val connectors: Seq[ActorRefRoutee] = settings.nodes.map {
    node =>
      val connectorRouter =
        context.actorOf(
          RoundRobinPool(settings.poolSize)
            .props(Props(new Connector(node))),
          "connector")

      ActorRefRoutee(connectorRouter)
  }

  private val topRouter = Router(
    RoundRobinRoutingLogic(),
    IndexedSeq(connectors: _*))

  private val manager = context.actorOf(
    Props(new ConnectionManager(topRouter)),
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