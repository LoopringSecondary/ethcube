package io.loopring.ethcube.endpoints

import akka.actor.ActorRef
import io.loopring.ethcube.common.json.JsonSupport
import javax.inject.{ Inject, Named }
import com.google.inject.{ Provides, Singleton }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import scala.concurrent.Future
import io.loopring.ethcube.model.JsonRpcRequest
import scala.util.Random
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import io.loopring.ethcube.model.JsonRpcResponse
import akka.actor.ActorSystem

@Provides @Singleton
class LooprEndpoints @Inject() (sys: ActorSystem, @Named("WorkerRoundRobinActor") actor: ActorRef) extends JsonSupport {

  import sys.dispatcher

  def apply(): Route = {
    pathPrefix("loopr") {
      pathEndOrSingleSlash {
        entity(as[Seq[JsonRpcRequest]]) { req ⇒
          // onSuccess(handleClientRequest(req)) { resp ⇒
          complete(req)
          // }
        }
      }
    }
  }

  //  implicit val timeout = Timeout(5 seconds)
  //
  //  def getTransactionReceipt(txHashs: Seq[String]): Future[Seq[JsonRpcResponse]] = {
  //    val json = JsonRpcRequest(id = 0, jsonrpc = "2.0", method = "eth_getTransactionReceipt", params = None)
  //    Future.sequence(txHashs.map { x ⇒ json.copy(id = Random.nextInt(100), params = Seq(x)) }.map { x ⇒ (actor ? x).mapTo[JsonRpcResponse] })
  //  }

}