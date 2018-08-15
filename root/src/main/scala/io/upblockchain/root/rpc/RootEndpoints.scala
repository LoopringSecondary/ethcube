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
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.actor.ActorSystem
import scala.util.Success
import scala.util.Failure
import akka.pattern.AskTimeoutException
import akka.util.ByteString
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import akka.stream.StreamTcpException

@Provides @Singleton
class RootEndpoints @Inject() (@Named("ClusterClient") cluster: ActorRef, sys: ActorSystem, mat: ActorMaterializer) extends JsonSupport {

  lazy val Log = LoggerFactory.getLogger(getClass)

  def myExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: AskTimeoutException ⇒
        Log.error(s"timeout: ${e.getMessage}", e)
        val r = HttpEntity(ContentTypes.`application/json`, ByteString("""{"jsonrpc":"2.0", "error": {"code": 500, "message": "akka ask timeout[5s]"}}"""))
        complete(HttpResponse(StatusCodes.InternalServerError, entity = r))
      case _: ArithmeticException ⇒
        extractUri { uri ⇒
          val r = HttpEntity(ContentTypes.`application/json`, ByteString(s"""{"jsonrpc":"2.0", "error": {"code": 500, "message": "bad request from : ${uri}"}}"""))
          complete(HttpResponse(StatusCodes.InternalServerError, entity = r))
        }
      case s: StreamTcpException ⇒
        Log.error(s"stream tcp exception: ${s.getMessage}", s)
        val r = HttpEntity(ContentTypes.`application/json`, ByteString(s"""{"jsonrpc":"2.0", "error": {"code": 500, "message": "stream exception : ${s.getMessage}"}}"""))
        complete(HttpResponse(StatusCodes.InternalServerError, entity = r))
      case t: Throwable ⇒
        complete("Ok")
    }

  def apply(): Route = {
    handleExceptions(myExceptionHandler) {
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
  }

  implicit val timeout = Timeout(5 seconds)

  private[this] def handleClientRequest(req: JsonRPCRequest): Future[JsonRPCResponse] = {
    (cluster ? req).mapTo[JsonRPCResponse]
  }
}
