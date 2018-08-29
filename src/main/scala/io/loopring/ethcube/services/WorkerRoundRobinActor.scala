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
      println("router ==>>>" + router)
      println("actor BroadcastRouter message to ")
    // router.route(akka.routing.Broadcast(s), sender)

    case Terminated(a) ⇒
    //      router = router.removeRoutee(a)
    //      val r = context.actorOf(Props[WorkerRoutee])
    //      context watch r
    //      router = router.addRoutee(r)
  }

}