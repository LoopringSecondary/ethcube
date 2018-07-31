package io.upblockchain.root.routees

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import javax.inject.Inject
import akka.actor.Actor

class RootRoute @Inject() (eth: EthJsonRPCRoute) {

  def apply(): Route = {
    index ~ eth()
  }

  def index: Route = {
    pathEndOrSingleSlash {
      complete("ok")
    }
  }

}