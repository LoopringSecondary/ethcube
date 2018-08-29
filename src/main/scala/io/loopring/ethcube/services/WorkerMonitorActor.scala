package io.loopring.ethcube.services

import akka.actor.Actor
import javax.inject.Inject
import javax.inject.Named
import akka.routing.Router
import akka.routing.Broadcast
import akka.actor.ActorSelection

case object Test1
case object Test2

class WorkerMonitorActor @Inject() (@Named("BroadcastRouter") router: Router) extends Actor {

  val i = new java.util.concurrent.atomic.AtomicInteger

  def receive: Actor.Receive = {
    case Test1 ⇒
      router.route(Broadcast("start"), sender)
    case Test2 ⇒
      println("receive =>> " + i.getAndIncrement + "===>>>")
      
      // router.removeRoutee(ActorSelection.apply(anchorRef, path))

  }

}