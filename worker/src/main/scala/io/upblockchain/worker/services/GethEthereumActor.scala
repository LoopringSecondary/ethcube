package io.upblockchain.worker.services

import akka.actor.ActorSystem
import io.upblockchain.worker.client.GethClient
import akka.stream.ActorMaterializer
import akka.actor.Actor

class GethEthereumActor(client: GethClient)(implicit system: ActorSystem, mat: ActorMaterializer) extends Actor {

  def receive: Actor.Receive = {
    case s: String ⇒ context.stop(self)
  }
}