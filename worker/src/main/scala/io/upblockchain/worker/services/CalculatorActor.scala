package io.upblockchain.worker.services

import akka.actor.Actor
import io.upblockchain.proto.hello._
import io.upblockchain.worker.client.GethClient
import javax.inject.Inject
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.RequestEntity
import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import scala.concurrent.duration._
import akka.stream.ActorMaterializer
import akka.pattern.pipe
import scala.util.Success
import scala.util.Failure
import akka.http.scaladsl.model.ContentType
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.util.ByteString

class CalculatorActor(client: GethClient)(implicit system: ActorSystem, mat: ActorMaterializer) extends Actor {

  import system.dispatcher
  val timeout = 300.millis

  val applicationJson = akka.http.scaladsl.model.headers.`Content-Type`(ContentTypes.`application/json`)

  def receive: Actor.Receive = {
    case s: HelloRequest ⇒
      val result = for {

        // requestEntity ← Marshal("""{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":83}""").to[RequestEntity]
        //        _ = HttpRequest.apply(method, uri, headers, entity, protocol)
        //        _ = requestEntity.contentType = applicationJson
        response ← client.handleRequest(HttpRequest(method = HttpMethods.POST, uri = Uri("/"),
          entity = HttpEntity.Strict(ContentTypes.`application/json`, ByteString("""{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":83}"""))))
        // json ← Unmarshal(response).to[HelloResponse]
        json ← response.entity.toStrict(timeout)(mat)
        _ = println("json ===>>>" + json)
      } yield {
        json.data.utf8String
      }

      //      result.onComplete {
      //        case Success(x) ⇒
      //          println("xxxx===>>>" + x)
      //          sender ! HelloResponse(x)
      //        case Failure(y) ⇒
      //          println("yyyy===>>>" + y)
      //          sender ! HelloResponse("can not complete")
      //      }

      result.map(HelloResponse(_)) pipeTo sender

    case _ ⇒ context.stop(self)
  }

}