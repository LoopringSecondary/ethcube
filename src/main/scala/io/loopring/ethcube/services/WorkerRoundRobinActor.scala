package io.loopring.ethcube.services

import akka.actor.Actor
import akka.routing.ActorRefRoutee
import akka.actor.Props
import akka.routing.RoundRobinRoutingLogic
import akka.routing.Router
import akka.actor.Terminated
import javax.inject.Inject
import javax.inject.Named
import io.loopring.ethcube.model.JsonRpcRequest

class WorkerRoundRobinActor @Inject() (@Named("RoundRobinRouter") router: Router) extends Actor {

  def receive: Actor.Receive = {
    case s: JsonRpcRequest ⇒
      // TODO(Toan) 这里做正常转发, 当有NoRoutee的时候还没处理
      router.route(s, sender)

    case Terminated(a) ⇒
    // TODO(Toan) 这里可以去掉
    //      router = router.removeRoutee(a)
    //      val r = context.actorOf(Props[WorkerRoutee])
    //      context watch r
    //      router = router.addRoutee(r)
  }

}