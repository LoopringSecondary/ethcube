package io.loopring.ethcube.model

case object BroadcastRequest
case class BroadcastResponse(label: String, isValid: Boolean = true)
