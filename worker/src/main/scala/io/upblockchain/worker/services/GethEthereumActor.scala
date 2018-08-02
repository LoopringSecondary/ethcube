package io.upblockchain.worker.services

import akka.actor.ActorSystem
import io.upblockchain.worker.client.GethClient
import akka.stream.ActorMaterializer
import akka.actor.Actor
import akka.http.scaladsl.marshalling.Marshal
import scala.concurrent.duration._
import io.upblockchain.common.json.JsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.pipe
import io.upblockchain.proto.jsonrpc._

class GethEthereumActor(client: GethClient)(implicit system: ActorSystem, mat: ActorMaterializer) extends Actor with JsonSupport {

  import system.dispatcher
  val timeout = 300.millis

  def receive: Actor.Receive = {
    case req: JsonRPCRequest ⇒
      val result = for {
        reqEntity ← Marshal(req).to[RequestEntity]
        httpResp ← client.handleRequest(HttpRequest(method = HttpMethods.POST, entity = reqEntity))
        jsonResp ← Unmarshal(httpResp).to[JsonRPCResponse]
      } yield jsonResp
      result pipeTo sender
    case _ ⇒ context.stop(self)
  }
}