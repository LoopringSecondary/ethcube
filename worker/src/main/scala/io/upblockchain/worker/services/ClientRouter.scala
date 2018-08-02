package io.upblockchain.worker.services

import akka.actor.{ Actor, ActorSystem, OneForOneStrategy, SupervisorStrategy }
import akka.routing.{ DefaultResizer, RoundRobinPool }
import akka.stream.ActorMaterializer

/*

  Copyright 2017 Loopring Project Ltd (Loopring Foundation).

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

*/
import scala.concurrent.duration._
import io.upblockchain.proto.jsonrpc._

class ClientRouter(ipcPath: String)(implicit system: ActorSystem, mat: ActorMaterializer) extends Actor {
  val routingDecider: PartialFunction[Throwable, SupervisorStrategy.Directive] = {
    case _: Exception ⇒ SupervisorStrategy.Restart
  }
  val routerSupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 5 seconds)(
    routingDecider.orElse(SupervisorStrategy.defaultDecider))
  val resizer = DefaultResizer(
    lowerBound = 2, upperBound = 50, pressureThreshold = 1, rampupRate = 1, backoffRate = 0.25, backoffThreshold = 0.25, messagesPerResize = 1)
  private val router = system.actorOf(
    RoundRobinPool(nrOfInstances = 2, resizer = Some(resizer), supervisorStrategy = routerSupervisorStrategy)
      .props(GethIpcRoutee.props(ipcPath)), "client-router")

  def receive: Actor.Receive = {
    case req: JsonRPCRequest ⇒
      router forward req
    case reqs: JsonRPCRequestSeq ⇒
      router forward reqs
    case _ ⇒
  }
}
