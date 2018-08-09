package io.upblockchain.root.rpc

import org.json4s.native.JsonMethods.{ parse }
import com.google.inject.{ Provides, Singleton }

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import io.upblockchain.common.json.JsonSupport
import io.upblockchain.common.model.JsonRPCRequestWrapped
import javax.inject.{ Inject, Named }
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import akka.actor.ActorRef
import akka.util.Timeout
import scala.concurrent.duration._
import io.upblockchain.proto.jsonrpc._
import scala.concurrent.Future
import akka.pattern.ask

@Provides @Singleton
class RootEndpoints @Inject() (@Named("ClusterClient") cluster: ActorRef, mat: ActorMaterializer) extends JsonSupport {

  lazy val Log = LoggerFactory.getLogger(getClass)

  def apply(): Route = {
    pathEndOrSingleSlash {
      entity(as[JsonRPCRequestWrapped]) { req ⇒
        Log.info(s"http request => ${req}")
        onSuccess(handleClientRequest(req.toRequest)) { resp ⇒
          Log.info(s"http response => ${resp.json}")
          complete(parse(resp.json))
        }
      }
    }
  }

  implicit val timeout = Timeout(5 seconds)

  private[this] def handleClientRequest(req: JsonRPCRequest): Future[JsonRPCResponse] = {
    (cluster ? req).mapTo[JsonRPCResponse]
  }
}
