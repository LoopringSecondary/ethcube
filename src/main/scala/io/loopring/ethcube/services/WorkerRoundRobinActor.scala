package io.loopring.ethcube.services

import akka.actor.Actor
import akka.routing.Router
import io.loopring.ethcube.model.JsonRpcRequest
import javax.inject.{ Inject, Named }

// 这里由 provide 方法创建 不需要注入
class WorkerRoundRobinActor(router: Router) extends Actor {

  def receive: Actor.Receive = {
    case s: JsonRpcRequest ⇒
      // TODO(Toan) 这里做正常转发, 当有NoRoutee的时候还没处理
      router.route(s, sender)
  }

}