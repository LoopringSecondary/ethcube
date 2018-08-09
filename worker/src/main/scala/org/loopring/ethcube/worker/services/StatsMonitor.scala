package org.loopring.ethcube.worker.services

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Address, Props }
import com.google.inject.name.Named
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
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
import org.loopring.ethcube.proto.jsonrpc._
import org.loopring.ethcube.common.model.JsonRPCRequestWrapped

class StatsMonitor(clientRouter: ActorRef)(implicit system: ActorSystem, materilizer: ActorMaterializer, timeout: Timeout) extends Actor with ActorLogging {

  import context.dispatcher
  context.system.scheduler.schedule(1 seconds, 10 seconds, self, CollectReq())
  val blockNumberReq = JsonRPCRequestWrapped("2.0", "eth_blockNumber", None, 1)

  var stat = Stat(self.path.address, 0, false)
  def receive: Actor.Receive = {
    case req: CollectReq ⇒ for {
      blockNumberRes ← clientRouter.ask(blockNumberReq.toRequest)
    } yield {
      blockNumberRes match {
        case res: JsonRPCResponse ⇒
        // println(res.result.get)
        //todo:
        //          stat = stat.copy(blockNumber = res.result.get)
      }
    }
    case req: String ⇒
    case _ ⇒
  }
}

case class CollectReq()

case class Stat(address: Address, blockNumber: Int, sycing: Boolean)

object StatsMonitor {
  def props(clientRouter: ActorRef)(implicit system: ActorSystem, materilizer: ActorMaterializer, timeout: Timeout) = Props(new StatsMonitor(clientRouter))
}