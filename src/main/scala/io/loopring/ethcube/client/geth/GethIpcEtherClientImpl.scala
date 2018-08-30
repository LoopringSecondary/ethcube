package io.loopring.ethcube.client.geth

import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.loopring.ethcube.modules.EtherClientConfig
import io.loopring.ethcube.client.EtherClient
import akka.actor.Actor

class GethIpcEtherClientImpl @Inject() (sys: ActorSystem, mat: ActorMaterializer, val config: EtherClientConfig) extends EtherClient with Actor {

  def receive: Actor.Receive = {
    case s: String ⇒ println("1111111")
  }

}