package io.upblockchain.worker.services

import akka.actor._
import io.upblockchain.common.json.JsonSupport
import io.upblockchain.proto.jsonrpc._
import akka.http.scaladsl.model._
import akka.util.ByteString
import io.upblockchain.worker.client.GethEthereumClient
import akka.stream.ActorMaterializer
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.pipe

class GethClientActor(client: GethEthereumClient)(implicit system: ActorSystem, mat: ActorMaterializer) extends Actor with JsonSupport {

  import system.dispatcher
  implicit val timeout = Timeout(5 seconds)

  def receive: Receive = {
    case req: JsonRPCRequest ⇒
      println("reewrwrwqrq======>>>>" + req.id)
      println("reewrwrwqrq======>>>>" + req.json)
      val httpReq = HttpRequest(
        method = HttpMethods.POST,
        entity = HttpEntity(ContentTypes.`application/json`, ByteString(req.json)))

      val result = for {
        httpResp ← client.handleRequest(httpReq)
        jsonResp ← httpResp.entity.dataBytes.map(_.utf8String).runReduce(_ + _)
      } yield JsonRPCResponse(id = req.id, json = jsonResp)

      println("reewrwrwqrq======>>>>" + result)
      result pipeTo sender

  }
}