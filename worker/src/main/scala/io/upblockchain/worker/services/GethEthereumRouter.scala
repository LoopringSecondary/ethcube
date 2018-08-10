package io.upblockchain.worker.services

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.actor.Actor
import akka.http.scaladsl.marshalling.Marshal
import scala.concurrent.duration._
import io.upblockchain.common.json.JsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.pipe
import io.upblockchain.proto.jsonrpc._
import io.upblockchain.common.model._
import akka.util.ByteString
import org.json4s.native.Serialization.{ write }
import akka.actor.ActorLogging
import scala.concurrent.Future
import io.upblockchain.worker.client.GethEthereumClient

class GethEthereumRouter(client: GethEthereumClient)(implicit system: ActorSystem, mat: ActorMaterializer) extends Actor with ActorLogging with JsonSupport {

  import system.dispatcher

  def receive: Actor.Receive = {
    case req: JsonRPCRequest ⇒
      // TODO(Toan) 这里可以试试修改为 forward actor 暂时先这样
      client.handleRequest(req) pipeTo sender
    case _ ⇒ context.stop(self)
  }

}