package io.loopring.ethcube.services

import akka.actor.Actor
import io.loopring.ethcube.model.JsonRpcRequest
import akka.routing.ConsistentHashingRoutingLogic
import javax.inject.Inject
import javax.inject.Named

class WorkerServiceRoutee extends Actor {

  /**
   * 分为以下几种情况:
   * 1、定时发送的消息, 直接请求客户端
   * 	1.1 异常情况，become
   * 	1.2 返回数据(更新中) become
   * 	1.3 返回数据(更新完毕)	unbecome
   *
   * 2、正常请求的消息
   * 	2.1 可用的情况下(直接请求客户端)
   *  2.2 不可用的情况下(转发)
   */
  def receive: Actor.Receive = {
    case s: JsonRpcRequest ⇒
      println("self ===>> " + context.self)
      sender ! s
    case s: String ⇒
      println("s ===>>>" + s + "###" + context.self)

      context.actorSelection("/user/WorkerMonitorActor") ! Test2
    // sender() ! Test2
  }

}