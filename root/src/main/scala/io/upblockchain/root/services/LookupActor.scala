package io.upblockchain.root.services

import akka.actor.Actor
import io.upblockchain.proto.hello.HelloRequest
import io.upblockchain.proto.hello.HelloResponse

class LookupActor extends Actor {

  def receive: Actor.Receive = {
    case i: Int ⇒
      context.actorSelection("akka.tcp://CalculatorSystem@127.0.0.1:2552/user/calculator") ! HelloRequest("Toan")
    case s: HelloResponse ⇒
      println("ss ==>>>" + s.message)
  }
}