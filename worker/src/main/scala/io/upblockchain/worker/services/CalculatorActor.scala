package io.upblockchain.worker.services

import akka.actor.Actor

class CalculatorActor extends Actor {

  def receive: Actor.Receive = {
    case s: String ⇒
      println("ss ==>>" + s)
      sender() ! "hello"
    case _ ⇒ context.stop(self)
  }

}