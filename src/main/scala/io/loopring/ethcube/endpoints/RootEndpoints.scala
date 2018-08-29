package io.loopring.ethcube.endpoints

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import io.loopring.ethcube.model.JsonRpcRequest
import io.loopring.ethcube.common.json.JsonSupport
import com.google.inject.{ Provides, Singleton }
import javax.inject.{ Inject, Named }
import akka.actor.ActorRef
import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

@Provides @Singleton
class RootEndpoints @Inject() (@Named("WorkerRoundRobinActor") actor: ActorRef) extends JsonSupport {

  def apply(): Route = {
    handleExceptions(myExceptionHandler) {
      pathEndOrSingleSlash {
        entity(as[JsonRpcRequest]) { req ⇒
          // Log.info(s"http request => ${req}")
          onSuccess(handleClientRequest(req)) { resp ⇒
            // Log.info(s"http response => ${resp.json}")
            complete(req)
          }
        }
      }
    }
  }

  implicit val timeout = Timeout(5 seconds)

  private[endpoints] def handleClientRequest(req: JsonRpcRequest): Future[JsonRpcRequest] = {
    // Future.successful(req)
    (actor ? req).mapTo[JsonRpcRequest]
  }

}