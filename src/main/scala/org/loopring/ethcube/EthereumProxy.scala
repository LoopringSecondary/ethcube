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

  private val requestRouter = new Router(
    RoundRobinRoutingLogic(),
    IndexedSeq(connectorGroups.map(g => ActorRefRoutee(g)): _*))

  private val manager = context.actorOf(
    Props(new ConnectionManager(
      requestRouter,
      connectorGroups,
      settings.checkIntervalSeconds,
      settings.healthyThreshold)),
    "ethereum_connector_manager")

  def receive: Receive = {
    case m: JsonRpcReq =>
      if (requestRouter.routees.isEmpty) {
        sender ! JsonRpcRes(
          id = None,
          jsonrpc = "2.0",
          error = Some(JsonRpcErr(600, "no routee")))
      } else {
        requestRouter.route(m, sender)
      }
  }
}