package io.upblockchain.root.services

import javax.inject.Inject
import akka.actor.ActorSystem
import scala.concurrent.Future
import akka.util.Timeout
import scala.concurrent.duration._
import akka.cluster.client.ClusterClient
import akka.cluster.client.ClusterClientSettings
import akka.pattern.ask
import javax.inject.Named
import akka.actor.ActorRef
import io.upblockchain.proto.jsonrpc.JsonRPCRequest
import io.upblockchain.proto.jsonrpc.JsonRPCResponse

class EthJsonRPCService @Inject() (@Named("ClusterClient") cluster: ActorRef) {

  implicit val timeout = Timeout(5 seconds)

  def handleClientRequest(req: JsonRPCRequest): Future[JsonRPCResponse] = {
    // TODO(Toan) 这里返回结果有问题
    // TODO(Toan) 地址可以进行配置化
    (cluster ? ClusterClient.Send("/user/GethActor", req, localAffinity = true)).mapTo[JsonRPCResponse]
  }

}