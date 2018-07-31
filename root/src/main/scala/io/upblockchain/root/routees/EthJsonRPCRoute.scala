package io.upblockchain.root.routees

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import javax.inject.Inject
import akka.actor.ActorSystem
import akka.util.Timeout

import scala.concurrent.duration._
import akka.actor.Props
import akka.cluster.client.ClusterClient
import akka.cluster.client.ClusterClientSettings
import io.upblockchain.proto.hello.HelloRequest
import akka.pattern.ask
import io.upblockchain.proto.hello.HelloResponse
import io.upblockchain.root.services.EthJsonRPCService

class EthJsonRPCRoute @Inject() (service: EthJsonRPCService) {

  lazy val prefix = "eth_"

  def apply(): Route = {
    get {
      path(s"${prefix}aa") {
        onSuccess(service.getBalance(HelloRequest("Test"))) { result ⇒
          complete(result.message)
        }
      } ~
        path(s"${prefix}bb") {
          complete("bb")
        }
    }
  }

}