package io.loopring.ethcube.model

import akka.actor.ActorRef

case object BroadcastRequest
case class BroadcastResponse(actor: ActorRef, isValid: Boolean = true)
