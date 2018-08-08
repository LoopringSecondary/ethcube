package io.upblockchain.root.services

import javax.inject.Inject
import akka.actor.ActorSystem
import scala.concurrent.Future
import akka.util.Timeout
import scala.concurrent.duration._
import akka.cluster.client._
import akka.pattern.ask
import javax.inject.Named
import akka.actor.ActorRef
import io.upblockchain.proto.jsonrpc._

class EthJsonRPCService @Inject() (@Named("ClusterClient") cluster: ActorRef) {

  implicit val timeout = Timeout(5 seconds)

  def handleClientRequest(req: JsonRPCRequest): Future[JsonRPCResponse] = {
    (cluster ? ClusterClient.Send("/user/ClientRouter", req, localAffinity = true)).mapTo[JsonRPCResponse]
  }

}