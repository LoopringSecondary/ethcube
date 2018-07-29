package io.upblockchain.worker

import akka.actor.ActorSystem
import io.upblockchain.worker.services.CalculatorActor
import akka.actor.Props
import com.typesafe.config.ConfigFactory

object Main extends App {

  implicit val system = ActorSystem("CalculatorSystem", ConfigFactory.load())
  system.actorOf(Props[CalculatorActor], "calculator")

}