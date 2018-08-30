package io.loopring.ethcube.services

import akka.actor.Actor
import javax.inject.Inject
import javax.inject.Named
import akka.routing.Router
import akka.routing.Broadcast
import akka.actor.ActorSelection
import io.loopring.ethcube.model.BroadcastRequest
import io.loopring.ethcube.model.BroadcastResponse

class WorkerMonitorActor(broadcastRouter: Router, roundRobinRouter: Router) extends Actor {

  val i = new java.util.concurrent.atomic.AtomicInteger

  def receive: Actor.Receive = {
    case BroadcastRequest ⇒
      println("1111111111111111")
      broadcastRouter.route(Broadcast(BroadcastRequest), sender)
    case s: BroadcastResponse ⇒
      println("receive =>> " + i.getAndIncrement + "===>>>" + s.label)
    //    case a: Any ⇒
    //      println("receive =>> " + i.getAndIncrement + "===>>>" + a)
    // println("receive =>> " + i.getAndIncrement + "===>>>")

    // roundRobinRouter.routees.size
    //      roundRobinRouter.add
    // TODO(Toan) 这里需要修改 判断逻辑
    // 去掉 routee 或者 做其他处理
    // 上面的参数需要修改
    // router.removeRoutee(ActorSelection.apply(anchorRef, path))

  }

}