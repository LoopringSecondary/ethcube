package io.loopring.ethcube.endpoints

import akka.actor.ActorRef
import io.loopring.ethcube.common.JsonSupport
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
import akka.http.scaladsl.model.HttpEntity
import akka.stream.ActorMaterializer

@Provides @Singleton
class LooprEndpoints @Inject() (
  sys: ActorSystem,
  mat: ActorMaterializer,
  @Named("WorkerControlerActor") actor: ActorRef)
  extends RootEndpoints(actor) {

  import sys.dispatcher

  implicit val m = mat

  override def apply(): Route = {
    handleExceptions(myExceptionHandler) {
      // 这里暂时固定, 可以修改为配置化的
      pathPrefix("loopr") {
        pathEndOrSingleSlash {
          // 数组模式
          entity(as[Seq[JsonRpcRequest]]) { reqs ⇒
            onSuccess(handleClientRequestSeq(reqs)) { complete(_) }
          }
        }
      }
    }
  }

  // 多请求
  def handleClientRequestSeq(reqs: Seq[JsonRpcRequest]): Future[Seq[JsonRpcResponse]] =
    Future.sequence(reqs.map(handleClientRequest))

}