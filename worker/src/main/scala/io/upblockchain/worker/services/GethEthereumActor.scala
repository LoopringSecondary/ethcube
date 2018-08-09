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
import io.upblockchain.worker.client.GethEthereumClient
import akka.actor.ActorLogging

class GethEthereumActor(client: GethEthereumClient)(implicit system: ActorSystem, mat: ActorMaterializer) extends Actor with ActorLogging with JsonSupport {

  import system.dispatcher

  def receive: Actor.Receive = {
    case req: JsonRPCRequest ⇒

      val httpReq = HttpRequest(
        method = HttpMethods.POST,
        entity = HttpEntity(ContentTypes.`application/json`, ByteString(req.json)))

      val result = for {
        httpResp ← client.handleRequest(httpReq)
        jsonResp ← httpResp.entity.dataBytes.map(_.utf8String).runReduce(_ + _)
        _ = log.info(s"geth client json response => ${jsonResp}")
      } yield JsonRPCResponse(id = req.id, json = jsonResp)

      result pipeTo sender

    case _ ⇒ context.stop(self)
  }
}