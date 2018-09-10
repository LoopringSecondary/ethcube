package org.loopring.ethcube.endpoints

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import org.loopring.ethcube.common.json.JsonSupport
import com.google.inject.{ Provides, Singleton }
import javax.inject.{ Inject, Named }
import akka.actor.ActorRef
import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import org.loopring.ethcube.model.JsonRpcRequest
import org.loopring.ethcube.model.JsonRpcResponse

@Provides @Singleton
class RootEndpoints @Inject() (@Named("WorkerControlerActor") actor: ActorRef) extends JsonSupport {

  def apply(): Route = {
    handleExceptions(myExceptionHandler) {
      pathEndOrSingleSlash {
        // json rpc
        entity(as[JsonRpcRequest]) { req ⇒
          onSuccess(handleClientRequest(req)) { complete(_) }
        }
      }
    }
  }

  implicit val timeout = Timeout(5 seconds)

  private[endpoints] def handleClientRequest(req: JsonRpcRequest): Future[JsonRpcResponse] = {
    (actor ? req).mapTo[JsonRpcResponse]
  }

}