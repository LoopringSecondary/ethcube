package io.loopring.ethcube.services

import akka.actor.{ Actor, ActorRef }
import javax.inject.{ Inject, Named }
import io.loopring.ethcube.model._
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.actor.Identify
import scala.concurrent.Future
import org.json4s.native.JsonMethods._
import org.slf4j.LoggerFactory

class WorkerServiceRoutee(client: ActorRef) extends Actor {

  lazy val Log = LoggerFactory.getLogger(getClass)

  import context.dispatcher

  implicit val timeout = Timeout(5 seconds)
  // 块最大差距
  lazy val blockCap = 20

  private def doRequest(req: JsonRpcRequest): Future[JsonRpcResponse] = (client ? req).mapTo[JsonRpcResponse]

  lazy val syncingJsonReq = JsonRpcRequest(id = 1, jsonrpc = "2.0", method = "eth_syncing", params = Seq.empty)

  lazy val monitorActorPath = "/user/WorkerMonitorActor"
  // 当前节点名称
  lazy val label = context.self.path.name

  /**
   * 分为以下几种情况:
   * 1、定时发送的消息, 直接请求客户端
   * 	1.1 异常情况，become
   * 	1.2 返回数据(更新中) become (这里可以使用become/unbecome 或者 使用removeRoutee)
   * 	1.3 返回数据(更新完毕)	unbecome
   *
   * 2、正常请求的消息
   * 	2.1 可用的情况下(直接请求客户端)
   *  2.2 不可用的情况下(转发)
   */
  def receive: Actor.Receive = {
    case req: JsonRpcRequest ⇒
      Log.info(s"WorkerRoutee normal forword json rpc request => ${req}")
      doRequest(req) pipeTo sender
    case BroadcastRequest ⇒
      Log.info(s"WorkerRoutee broadcast Request")
      doRequest(syncingJsonReq).map { resp ⇒
        resp.result.map { result ⇒
          Log.info(s"WorkerRoutee[${label}] get eth syning response => ${resp}")
          try {
            val jsonMap = result.asInstanceOf[Map[String, Any]]
            // 其他字段不做处理
            val currentBlock = anyToBigInt((jsonMap.get("currentBlock")))
            val highestBlock = anyToBigInt((jsonMap.get("highestBlock")))

            Log.debug(s"WorkerRoutee[${label}] get eth syning block { currentBlock: ${currentBlock}, highestBlock:${highestBlock} }")

            if (currentBlock + blockCap < highestBlock) {
              Log.info(s"WorkerRoutee[${label}] check client failed to achieve the highest block, { currentBlock: ${currentBlock}, highestBlock:${highestBlock} }")
              // 当前的高度不够高的话 WorkerRoutee 需要移出
              context.actorSelection(monitorActorPath) ! BroadcastResponse(label = label, isValid = false)
            } else {
              Log.info(s"WorkerRoutee[${label}] check successfuled, { currentBlock: ${currentBlock}, highestBlock:${highestBlock} }")
              context.actorSelection(monitorActorPath) ! BroadcastResponse(label = label)
            }

          } catch {
            case ex: Exception ⇒
              Log.error(s"syning parse json response error: ${resp}", ex)
          }
        }
      }
  }

  def anyToBigInt: PartialFunction[Option[Any], BigInt] = {
    case Some(s) ⇒
      // 去掉 0x
      BigInt(s.asInstanceOf[String].substring(2), 16)
    case _ ⇒ BigInt(0)
  }

  // parity client demo
  //{
  //  "jsonrpc": "2.0",
  //  "result": {
  //      "currentBlock": "0x5e9bec",
  //      "highestBlock": "0x5f3850",
  //      "startingBlock": "0x5e9bec",
  //      "warpChunksAmount": "0x7bb",
  //      "warpChunksProcessed": "0x559"
  //  },
  //  "id": 1
  //}

  // geth client demo
  //{
  //  "jsonrpc": "2.0",
  //  "id": 1,
  //  "result": {
  //      "currentBlock": "0x5f0c82",
  //      "highestBlock": "0x5f3408",
  //      "knownStates": "0xbe1ac12",
  //      "pulledStates": "0xbe1ac12",
  //      "startingBlock": "0x5f0755"
  //    }
  //}

}