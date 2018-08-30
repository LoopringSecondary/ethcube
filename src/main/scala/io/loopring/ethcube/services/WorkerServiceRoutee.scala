package io.loopring.ethcube.services

import akka.actor.Actor
import akka.routing.ConsistentHashingRoutingLogic
import javax.inject.Inject
import javax.inject.Named
import akka.actor.ActorRef
import io.loopring.ethcube.model.BroadcastRequest
import io.loopring.ethcube.model.BroadcastResponse
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.actor.Identify
import io.loopring.ethcube.model.JsonRpcRequest
import io.loopring.ethcube.model.JsonRpcResponse
import scala.concurrent.Future
import org.json4s.native.JsonMethods._
import org.json4s.JsonAST.JValue
import org.json4s.JsonAST.JString
import scala.collection.immutable.Map

class WorkerServiceRoutee(client: ActorRef) extends Actor {

  import context.dispatcher

  implicit val timeout = Timeout(5 seconds)

  lazy val blockCap = 20

  private def doRequest(req: JsonRpcRequest): Future[JsonRpcResponse] = (client ? req).mapTo[JsonRpcResponse]

  lazy val syncingJsonReq = JsonRpcRequest(id = 1, jsonrpc = "2.0", method = "eth_syncing", params = Seq.empty)

  lazy val monitorActorPath = "/user/WorkerMonitorActor"

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
    case req: JsonRpcRequest ⇒ doRequest(req) pipeTo sender
    case BroadcastRequest ⇒

      println("22222222222222")

      doRequest(syncingJsonReq).map { resp ⇒
        // TODO(Toan) 这里要检测 result 类型

        resp.result.map { result ⇒

          try {
            val jsonMap = result.asInstanceOf[Map[String, Any]]

            val currentBlock = anyToBigInt((jsonMap.get("currentBlock")))
            val highestBlock = anyToBigInt((jsonMap.get("highestBlock")))

            println("currentBlock ==>>" + currentBlock)
            println("highestBlock ==>>" + highestBlock)

            if (currentBlock + blockCap <= highestBlock) {
              println("1231231231")

              context.actorSelection(monitorActorPath) ! BroadcastResponse(label = context.self.path.name)
            }

          } catch {
            case ex: Exception ⇒ println("exceotuib::" + ex.getMessage)
          }

        }
      }
  }

  def anyToBigInt: PartialFunction[Option[Any], BigInt] = {
    case Some(s) ⇒ BigInt(s.asInstanceOf[String].substring(2), 16)
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