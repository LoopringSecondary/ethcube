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

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit._
import org.scalatest._
import org.loopring.accessor._
import org.loopring.ethcube.proto.eth_jsonrpc._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class HttpConnectorSpec() extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  info("execute cmd [sbt ethcube/'testOnly *HttpConnectorSpec'] test ethereum http connector receive")

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "ethBlockNumberReq" in {
    val resultFuture = for {
      resp ← connector ? EthBlockNumberReq()
    } yield resp

    val result = Await.result(resultFuture, timeout.duration)
    info(result.toString)
  }
}
