package io.upblockchain.root.endpoints

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import org.json4s.native.JsonMethods.parse
import org.slf4j.LoggerFactory

import com.google.inject.{ Provides, Singleton }

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import io.upblockchain.common.json.JsonSupport
import io.upblockchain.common.model.JsonRPCRequestWrapped
import io.upblockchain.proto.jsonrpc._
import javax.inject.{ Inject, Named }
import akka.pattern.ask

@Provides @Singleton
class RootEndpoints @Inject() (@Named("ClusterClient") cluster: ActorRef, sys: ActorSystem, mat: ActorMaterializer) extends JsonSupport {

  lazy val Log = LoggerFactory.getLogger(getClass)

  def apply(): Route = {
    handleExceptions(myExceptionHandler) {
      pathEndOrSingleSlash {
        entity(as[JsonRPCRequestWrapped]) { req ⇒
          Log.info(s"http request => ${req}")
          // complete("Ok")
          onSuccess(handleClientRequest(req.toRequest)) { resp ⇒
            Log.info(s"http response => ${resp.json}")
            complete(parse(resp.json))
          }
        }
      }
    }
  }

  implicit val timeout = Timeout(5 seconds)

  private[this] def handleClientRequest(req: JsonRPCRequest): Future[JsonRPCResponse] = {
    (cluster ? req).mapTo[JsonRPCResponse]
  }
}
