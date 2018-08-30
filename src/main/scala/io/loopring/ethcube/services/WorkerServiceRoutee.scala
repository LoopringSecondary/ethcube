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

class WorkerServiceRoutee(client: ActorRef) extends Actor {

  import context.dispatcher

  implicit val timeout = Timeout(5 seconds)

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
      doRequest(syncingJsonReq).map { resp ⇒
        // TODO(Toan) 这里要检测 result 类型
        // resp.result
        context.actorSelection(monitorActorPath) ! BroadcastResponse(label = context.self.path.name)

      }
  }

}