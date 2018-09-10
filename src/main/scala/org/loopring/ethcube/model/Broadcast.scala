package org.loopring.ethcube.model

import akka.actor.ActorRef

// 广播
case object BroadcastRequest
case class BroadcastResponse(actor: ActorRef, isValid: Boolean = true)
