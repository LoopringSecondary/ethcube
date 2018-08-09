package io.upblockchain.root.router

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

import akka.actor.ActorSystem
import akka.dispatch.Dispatchers
import akka.routing.{ Group, Router }
import com.typesafe.config.Config
import scala.collection.immutable
import akka.japi.Util.immutableSeq

final case class EthWorkerGroup(routeePaths: immutable.Iterable[String]) extends Group {

  override def paths(system: ActorSystem): immutable.Iterable[String] = routeePaths

  override def createRouter(system: ActorSystem): Router =
    new Router(new EthRoutingLogic())

  override val routerDispatcher: String = Dispatchers.DefaultDispatcherId
}