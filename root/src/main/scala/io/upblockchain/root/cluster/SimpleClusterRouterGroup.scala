package io.upblockchain.root.cluster

import akka.routing.Group
import akka.actor.ActorSystem
import akka.routing.Router
import scala.collection.immutable

class SimpleClusterRouterGroup extends Group {

  def createRouter(system: ActorSystem): Router = {
    ???
  }

  def paths(system: ActorSystem): immutable.Iterable[String] = {
    ???
  }

  def routerDispatcher: String = {
    ???
  }

}