package io.loopring.ethcube.services

import akka.routing.Router
import akka.actor.Actor
import io.loopring.ethcube.model.JsonRpcRequest
import org.slf4j.LoggerFactory
import io.loopring.ethcube.model.{ BroadcastRequest, BroadcastResponse }
import akka.actor.ActorRef
import akka.routing.Routee
import akka.routing.ActorRefRoutee
import akka.routing.Broadcast
import io.loopring.ethcube.model.JsonRpcResponse
import io.loopring.ethcube.model.JsonRpcError

class WorkerControlerActor(broadcastRouter: Router, var roundRobinRouter: Router) extends Actor {

  lazy val Log = LoggerFactory.getLogger(getClass)

  def receive: Actor.Receive = {
    case s: JsonRpcRequest ⇒
      if (roundRobinRouter.routees.isEmpty)
        sender ! JsonRpcResponse(id = None, jsonrpc = "2.0", error = Some(JsonRpcError(500, "has no routee")))
      else roundRobinRouter.route(s, sender)

    case BroadcastRequest ⇒ broadcastRouter.route(Broadcast(BroadcastRequest), sender)
    case BroadcastResponse(actor, isValid) ⇒ addOrRemoveRoutee(actor, isValid)
  }

  def getRoutee(actor: ActorRef): Option[Routee] = {
    val r = ActorRefRoutee(actor)
    roundRobinRouter.routees.find(_ == r)
  }

  def addOrRemoveRoutee(actor: ActorRef, isValid: Boolean): Unit = {

    val label = actor.path.name

    getRoutee(actor) match {
      case Some(r) ⇒
        if (isValid) Log.info(s"WorkerRoutee[${label}] is running, checked successful!")
        else {
          Log.info(s"WorkerRoutee[${label}] is running, remove from RoundRobinRouter")
          roundRobinRouter = roundRobinRouter.removeRoutee(r)
          Log.debug(s"WorkerRoutee[${label}] => ${roundRobinRouter.routees}")
        }
      case _ ⇒
        if (isValid) {
          Log.info(s"WorkerRoutee[${label}] is not running and add to RoundRobinRouter")
          roundRobinRouter = roundRobinRouter.addRoutee(ActorRefRoutee(actor))
          Log.debug(s"WorkerRoutee[${label}] => ${roundRobinRouter.routees}")
        } else Log.info(s"WorkerRoutee[${label}] is not running, routee has bean removed")
    }
  }

}