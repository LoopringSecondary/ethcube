package io.upblockchain.worker.services

import akka.actor.ActorSystem
import io.upblockchain.worker.client.GethClient
import akka.stream.ActorMaterializer
import akka.actor.Actor
import io.upblockchain.proto.jsonrpc.JsonRPCRequest
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.RequestEntity
import scala.concurrent.duration._
import io.upblockchain.common.json.JsonSupport
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import io.upblockchain.proto.jsonrpc.JsonRPCResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.pipe

class GethEthereumActor(client: GethClient)(implicit system: ActorSystem, mat: ActorMaterializer) extends Actor with JsonSupport {

  import system.dispatcher
  val timeout = 300.millis

  def receive: Actor.Receive = {
    case req: JsonRPCRequest ⇒
      // TODO(Toan):  这里的result是 Any 类型的 不能自动映射, 需要修改程序
      val result = for {
        reqEntity ← Marshal(req).to[RequestEntity]
        httpResp ← client.handleRequest(HttpRequest(method = HttpMethods.POST, entity = reqEntity))
        //        x ← Unmarshal(httpResp.entity).to[String]
        //        _ = println("xxxx===>>>" + x)
        jsonResp ← Unmarshal(httpResp).to[JsonRPCResponse]
      } yield jsonResp

      result pipeTo sender

    case _ ⇒ context.stop(self)
  }
}