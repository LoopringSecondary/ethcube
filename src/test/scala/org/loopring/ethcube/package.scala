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

package org.loopring

import akka.actor.{ ActorSystem, Props }
import akka.stream.ActorMaterializer
import akka.util.Timeout
import org.loopring.ethcube.HttpConnector
import org.loopring.ethcube.proto.data.EthereumProxySettings

import scala.concurrent.duration._

package object accessor {

  val node = EthereumProxySettings.Node(
    host = "127.0.0.1",
    port = 8545
  )

  implicit val system = ActorSystem("ethcube")
  implicit val materializer = ActorMaterializer()
  implicit val timeout = Timeout(100 second)

  val connector = system.actorOf(Props(new HttpConnector(node)), "connector")
}
