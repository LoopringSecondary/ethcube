package io.upblockchain.root.routing

import akka.routing.{ Routee, RoutingLogic }
import scala.collection.immutable
import akka.routing.RoundRobinRoutingLogic
import akka.routing.SeveralRoutees

class SimpleRoutingLogic extends RoutingLogic {

  //  def select(message: Any, routees: immutable.IndexedSeq[Routee]): Routee = {
  //    ???
  //  }

  val nbrCopies: Int = 10
  val roundRobin = RoundRobinRoutingLogic()
  def select(message: Any, routees: immutable.IndexedSeq[Routee]): Routee = {
    val targets = (1 to nbrCopies).map(_ ⇒ roundRobin.select(message, routees))
    SeveralRoutees(targets)
  }

}