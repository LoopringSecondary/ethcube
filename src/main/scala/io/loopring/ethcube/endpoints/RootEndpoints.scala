package io.loopring.ethcube.endpoints

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import io.loopring.ethcube.common.json.JsonSupport
import com.google.inject.{ Provides, Singleton }
import javax.inject.{ Inject, Named }
import akka.actor.ActorRef
import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import io.loopring.ethcube.model.JsonRpcRequest

@Provides @Singleton
class RootEndpoints @Inject() (@Named("WorkerRoundRobinActor") actor: ActorRef) extends JsonSupport {

  def apply(): Route = {
    // TODO(Toan) 这里需要添加日志
    handleExceptions(myExceptionHandler) {
      pathEndOrSingleSlash {
        entity(as[JsonRpcRequest]) { req ⇒
          onSuccess(handleClientRequest(req)) { resp ⇒
            complete(resp)
          }
        }
      }
    }
  }

  implicit val timeout = Timeout(5 seconds)

  private[endpoints] def handleClientRequest(req: JsonRpcRequest): Future[JsonRpcRequest] = {
    (actor ? req).mapTo[JsonRpcRequest]
  }

}