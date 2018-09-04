package io.loopring.ethcube.services

import akka.actor.{ Actor, ActorRef }
import org.slf4j.LoggerFactory
import io.loopring.ethcube.model.{ BroadcastRequest, BroadcastResponse }
import akka.routing._
import io.loopring.ethcube.model.{ JsonRpcRequest, JsonRpcResponse, JsonRpcError }

class WorkerControllerActor(broadcastRouter: Router, var roundRobinRouter: Router) extends Actor {

  lazy val Log = LoggerFactory.getLogger(getClass)

  def receive: Actor.Receive = {
    // json rpc
    case s: JsonRpcRequest ⇒
      if (roundRobinRouter.routees.isEmpty)
        sender ! JsonRpcResponse(id = None, jsonrpc = "2.0", error = Some(JsonRpcError(600, "has no routee")))
      else roundRobinRouter.route(s, sender)

    // 广播请求
    case BroadcastRequest ⇒ broadcastRouter.route(Broadcast(BroadcastRequest), sender)
    // 广播响应
    case BroadcastResponse(actor, isValid) ⇒ addOrRemoveRoutee(actor, isValid)
  }

  private[services] def getRoutee(actor: ActorRef): Option[Routee] = {
    val r = ActorRefRoutee(actor)
    roundRobinRouter.routees.find(_ == r)
  }

  private[services] def addOrRemoveRoutee(actor: ActorRef, isValid: Boolean): Unit = {

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