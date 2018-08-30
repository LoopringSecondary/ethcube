package io.loopring.ethcube.services

import akka.actor.Actor
import io.loopring.ethcube.model.JsonRpcRequest
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

class WorkerServiceRoutee(sys: ActorSystem, client: ActorRef) extends Actor {

  import sys.dispatcher

  implicit val timeout = Timeout(5 seconds)
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
    case s: JsonRpcRequest ⇒ sender ! s
    case s: String ⇒
      println("hahahah ==>>")
      // TODO(Toan) 这里需要返回数据
      val d = (client ? JsonRpcRequest(id = 1, jsonrpc = "", method = "", params = ""))

    // d pipeTo sys.actorSelection("") //.resolveOne()

    // sys.actorSelection(path)

    // sys.actorSelection("/user/WorkerMonitorActor") !
    // pipeTo sender
    // client ? ""
    // TODO(Toan) 完善上面的逻辑
    // println("s ===>>>" + "###" + context.self.path)
    // ! BroadcastResponse
    // sender() ! Test2
  }

}