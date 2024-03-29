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

import scala.concurrent._
import akka.pattern.ask
import akka.actor._
import akka.util.Timeout
import akka.routing._
import org.loopring.ethcube.proto.data._
import scala.concurrent.duration._
import scala.util.Random
import org.json4s.native.JsonMethods._
import org.json4s.DefaultFormats
import javax.swing.Spring.HeightSpring

private[ethcube] class ConnectionManager(
    requestRouterActor: ActorRef,
    connectorGroups: Seq[ActorRef],
    checkIntervalSeconds: Int,
    healthyThreshold: Float
)
  extends Actor
  with ActorLogging {

  implicit val ec = context.system.dispatcher
  implicit val timeout: Timeout = Timeout(1 seconds)
  private val size = connectorGroups.size
  implicit val formats = DefaultFormats

  private val errCheckBlockHeightResp =
    CheckBlockHeightResp(currentBlock = 1, heightBlock = 0)

  context.system.scheduler.schedule(
    checkIntervalSeconds.seconds,
    checkIntervalSeconds.seconds,
    self,
    CheckBlockHeight()
  )

  /** updated date: 2018-9-12 by Toan
   *
   *  1、google.protobuf.Any 在序列化和反序列化 是需要对 Any 内部的 typeUrl 和 value字段进行处理
   *  暂时没找到合适的处理方式
   *  link (https://github.com/scalapb/ScalaPB/blob/master/third_party/google/protobuf/any.proto)
   *
   *  2、修改了判断块高的逻辑, 当geth/parity完全同步的时候 返回的 result=false
   *  TODO(Toan) 如果这里面要想获取 当前块 应该配合 eth_blockNumber
   *
   *  3、把环形路由变成actor, 这样可以对路由actor发送消息
   *  link (https://doc.akka.io/docs/akka/current/routing.html#management-messages)
   */
  def receive: Receive = {

    case m: CheckBlockHeight ⇒
      log.info("start scheduler check highest block...")
      val syncingJsonRpcReq = JsonRpcReqWrapped(
        id = Random.nextInt(100),
        jsonrpc = "2.0",
        method = "eth_syncing",
        params = None
      )
      val blockNumJsonRpcReq = JsonRpcReqWrapped(
        id = Random.nextInt(100),
        jsonrpc = "2.0",
        method = "eth_blockNumber",
        params = None
      )
      import JsonRpcResWrapped._
      for {
        resps: Seq[(ActorRef, CheckBlockHeightResp)] ← Future.sequence(
          connectorGroups.map { g ⇒
            for {
              syncingResp ← (g ? syncingJsonRpcReq.toPB)
                .mapTo[JsonRpcRes]
                .map(toJsonRpcResWrapped)
                .map(_.result)
                .map(toCheckBlockHeightResp)
                .recover {
                  case e: TimeoutException ⇒
                    log.error(
                      s"timeout on getting blockheight: $g: ${e.getMessage}"
                    )
                    errCheckBlockHeightResp
                  case e: Throwable ⇒
                    log.error(
                      s"exception on getting blockheight: $g: ${e.getMessage}"
                    )
                    errCheckBlockHeightResp
                }
              // get each node block number
              blockNumResp ← (g ? blockNumJsonRpcReq.toPB)
                .mapTo[JsonRpcRes]
                .map(toJsonRpcResWrapped)
                .map(_.result)
                .map(anyHexToInt)
              // heightBlcok = if(!syncing) currentBlock else syncing['height_block']
            } yield {
              val heightBlock = Seq(syncingResp.heightBlock, blockNumResp).max
              log.info(
                s"{ currentBlock: ${blockNumResp}, highestBlock: ${heightBlock} } @ ${g.path}"
              )
              (
                g,
                syncingResp
                .copy(currentBlock = blockNumResp, heightBlock = heightBlock)
              )
            }
          }
        )
        heightBNInGroup = resps.map(_._2.heightBlock).max
        goodGroupsOption = Seq(10, 20, 30)
          .map { i ⇒
            // 计算最高块和当前块的差距
            resps.filter(x ⇒ heightBNInGroup - x._2.currentBlock <= i).map(_._1)
          }
          .find(_.size >= size * healthyThreshold)
      } yield {
        // remove all routees
        connectorGroups.foreach { g ⇒
          // val r = ActorSelectionRoutee(context.actorSelection(g.path))
          requestRouterActor ! RemoveRoutee(ActorRefRoutee(g))
        }
        // only add Some(routee)
        goodGroupsOption.foreach {
          _.foreach { g ⇒
            log.info(s"added to connectorGroup: ${g.path}")
            val r = ActorSelectionRoutee(context.actorSelection(g.path))
            requestRouterActor ! AddRoutee(r)
          }
        }

        log.info(
          s"GoodGroups: ${goodGroupsOption.map(_.size).getOrElse(0)} connectorGroup are still in good shape, " +
            s"in connectorGroup height block number: ${heightBNInGroup}, end scheduler"
        )
      }
  }

  def toCheckBlockHeightResp: PartialFunction[Any, CheckBlockHeightResp] = {
    case m: Map[_, _] ⇒
      val currentBlock =
        m.find(_._1 == "currentBlock").map(_._2).map(anyHexToInt).getOrElse(0)
      val heightBlock =
        m.find(_._1 == "highestBlock").map(_._2).map(anyHexToInt).getOrElse(10)
      CheckBlockHeightResp(currentBlock, heightBlock)
    case b: Boolean ⇒
      if (b) errCheckBlockHeightResp else CheckBlockHeightResp(1, 10)
    case _ ⇒ errCheckBlockHeightResp
  }

  def anyHexToInt: PartialFunction[Any, Int] = {
    case s: String ⇒ BigInt(s.replace("0x", ""), 16).toInt
    case _         ⇒ 0
  }

}
