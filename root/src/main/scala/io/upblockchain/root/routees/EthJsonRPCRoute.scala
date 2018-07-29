package io.upblockchain.root.routees

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

class EthJsonRPCRoute {

  lazy val prefix = "eth_"

  def apply(): Route = {
    get {
      path(s"${prefix}aa") {
        complete("aa")
      }
      path(s"${prefix}bb") {
        complete("bb")
      }
    }
  }

}