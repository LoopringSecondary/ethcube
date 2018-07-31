package io.upblockchain.root.services

import javax.inject.Inject
import akka.actor.ActorSystem
import scala.concurrent.Future
import io.upblockchain.proto.hello._
import akka.util.Timeout
import scala.concurrent.duration._
import akka.cluster.client.ClusterClient
import akka.cluster.client.ClusterClientSettings
import akka.pattern.ask
import javax.inject.Named
import akka.actor.ActorRef

class EthJsonRPCService @Inject() (@Named("ClusterClient") cluster: ActorRef) {

  implicit val timeout = Timeout(5 seconds)

  def getBalance(req: HelloRequest): Future[HelloResponse] = {
    // val actorRef = system.actorOf(ClusterClient.props(ClusterClientSettings(system)), "test")
    (cluster ? ClusterClient.Send("/user/calculator", HelloRequest("Test"), localAffinity = true)).mapTo[HelloResponse]
  }

}