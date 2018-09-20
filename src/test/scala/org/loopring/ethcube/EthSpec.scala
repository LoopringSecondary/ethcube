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

class EthSpec()
  extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  info("execute cmd [sbt ethcube/'testOnly *EthSpec'] to test all")

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "ethBlockNumberReq" in {
    info("cmd [sbt ethcube/'testOnly *EthSpec -- -z ethBlockNumberReq']")

    val req = EthBlockNumberReq()
    val resFuture = connector ? req
    val res = Await.result(resFuture, timeout.duration)
    info(res.toString)
  }

  "EthGetBalanceReq" in {
    info("cmd [sbt ethcube/'testOnly *EthSpec -- -z EthGetBalanceReq']")

    val req = EthGetBalanceReq(
      address = "0x4bad3053d574cd54513babe21db3f09bea1d387d",
      tag = "latest"
    )
    val resFuture = connector ? req
    val res = Await.result(resFuture, timeout.duration)
    info(res.toString)
  }

  "GetTransactionByHashReq" in {
    info("cmd [sbt ethcube/'testOnly *EthSpec -- -z GetTransactionByHashReq']")

    val req = GetTransactionByHashReq(
      "0x8bc1e170941db9c481e13aefe82f237dc3e18500550974a6bc43bacb6e0cc35b"
    )
    val resFuture = connector ? req
    val res = Await.result(resFuture, timeout.duration)
    info(res.toString)
  }

  "GetTransactionReceiptReq" in {
    info("cmd [sbt ethcube/'testOnly *EthSpec -- -z GetTransactionReceiptReq']")

    val req = GetTransactionReceiptReq(
      "0x8bc1e170941db9c481e13aefe82f237dc3e18500550974a6bc43bacb6e0cc35b"
    )
    val resFuture = connector ? req
    val res = Await.result(resFuture, timeout.duration)
    info(res.toString)
  }

  "TraceTransactionReq" in {
    info("cmd [sbt ethcube/'testOnly *EthSpec -- -z TraceTransactionReq']")

    val req = TraceTransactionReq(
      "0x8bc1e170941db9c481e13aefe82f237dc3e18500550974a6bc43bacb6e0cc35b"
    )
    val resFuture = connector ? req
    val res = Await.result(resFuture, timeout.duration)
    info(res.toString)
  }

  "GetBlockWithTxHashByNumberReq" in {
    info(
      "cmd [sbt ethcube/'testOnly *EthSpec -- -z GetBlockWithTxHashByNumberReq']"
    )

    val blockNumber = "0x" + BigInt(43161).intValue().toHexString
    info(s"blockNumber is $blockNumber")

    val req = GetBlockWithTxHashByNumberReq(blockNumber)
    val resFuture = connector ? req
    val res = Await.result(resFuture, timeout.duration)
    info(res.toString)
  }

  "GetNonceReq" in {
    info("cmd [sbt ethcube/'testOnly *EthSpec -- -z GetNonceReq']")

    val req =
      GetNonceReq("0xb1018949b241d76a1ab2094f473e9befeabb5ead", "pending")
    val resFuture = connector ? req
    val res = Await.result(resFuture, timeout.duration)
    info(res.toString)
  }

  //curl http://127.0.0.1:8545/ -X POST -H "Content-Type: application/json" -d '{"jsonrpc":"2.0","method":"eth_estimateGas","params": [{"to":"0xcd36128815ebe0b44d0374649bad2721b8751bef", "data":"0xa9059cbb0000000000000000000000005ecd4ea8f7d146ddb0d5ed61ab83ab7d4759b122000000000000000000000000000000000000000000000878678326eac9000000"}],"id":1}'
  "GetEstimatedGasReq" in {
    info("cmd [sbt ethcube/'testOnly *EthSpec -- -z GetEstimatedGasReq']")

    val lrc = "0xcd36128815ebe0b44d0374649bad2721b8751bef"
    val transfer =
      "0xa9059cbb000000000000000000000000b1018949b241d76a1ab2094f473e9befeabb5ead000000000000000000000000000000000000000000000878678326eac9000000"
    val req = GetEstimatedGasReq(lrc, transfer)
    val resFuture = connector ? req
    val res = Await.result(resFuture, timeout.duration)
    info(res.toString)
  }
}
