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

import java.util

import akka.testkit.ImplicitSender
import org.scalatest.BeforeAndAfterAll
import akka.testkit.TestKit
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import akka.actor.{ ActorSystem, Props }
import org.loopring.accessor._
import org.loopring.ethcube.proto.eth_jsonrpc._
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import akka.pattern.ask
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.loopring.ethcube.proto.data.EthereumProxySettings

import scala.concurrent.Await

class EthCallSpec
  extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  implicit val materializer = ActorMaterializer()
  import collection.JavaConverters._

  val config = ConfigFactory.load()
  val settings: EthereumProxySettings = {
    val sub = config.getConfig("ethereum-proxy")

    EthereumProxySettings(
      sub.getInt("pool-size"),
      sub.getInt("check-interval-seconds"),
      sub.getDouble("healthy-threshold").toFloat,
      sub.getConfigList("nodes").asScala map { c ⇒
        EthereumProxySettings
          .Node(c.getString("host"), c.getInt("port"), c.getString("ipcpath"))
      }
    )
  }

  val proxy = system.actorOf(Props(new EthereumProxy(settings)), "ethereum_proxy")

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "ethCallBalanceOf" in {

    val data = abiFunction("balanceOf")(
      Some("0x1b978a1d302335a6f2ebe4b8823b5e17c3c84135")
    )
    println("data => " + functionToHex(data))
    val args = TransactionParam()
      .withTo("0xcd36128815ebe0b44d0374649bad2721b8751bef")
      .withData(data)

    val req = EthCallReq().withTag("latest").withParam(args)

    val resFuture = proxy ? req
    val res = Await.result(resFuture, timeout.duration)

    println(res.toString)

    info(res.toString)

  }

  implicit def functionToHex: PartialFunction[org.web3j.abi.datatypes.Function, String] = {
    case f: org.web3j.abi.datatypes.Function ⇒
      FunctionEncoder.encode(f)
  }

  def abiFunction(
    method: String
  )(owner: Option[String] = None): org.web3j.abi.datatypes.Function = {
    import scala.collection.JavaConverters._
    val types: List[org.web3j.abi.datatypes.Type[_]] = owner match {
      case Some(o) ⇒ List(new Address(o))
      case _       ⇒ List.empty[Address]
    }

    new org.web3j.abi.datatypes.Function(
      method,
      types.asJava,
      new util.ArrayList()
    )
  }

}
