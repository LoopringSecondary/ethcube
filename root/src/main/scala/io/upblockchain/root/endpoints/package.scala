package io.upblockchain.root

import akka.http.scaladsl.server.ExceptionHandler
import akka.pattern.AskTimeoutException
import akka.http.scaladsl.model._
import akka.util.ByteString
import akka.stream.StreamTcpException
import akka.http.scaladsl.server.Directives._
import org.slf4j.LoggerFactory

package object endpoints {

  lazy val Log = LoggerFactory.getLogger(getClass)

  def myExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: AskTimeoutException ⇒
        Log.error(s"timeout: ${e.getMessage}", e)
        complete(errorHttpResponse("akka ask timeout[5s]"))
      case e: ArithmeticException ⇒
        extractUri { uri ⇒
          Log.error(s"bad request from : ${uri}", e)
          complete(errorHttpResponse(s"bad request from : ${uri}"))
        }
      case e: StreamTcpException ⇒
        Log.error(s"stream exception : ${e.getMessage}", e)
        complete(errorHttpResponse(s"stream exception : ${e.getMessage}"))
      case t: Throwable ⇒
        complete(errorHttpResponse(s"Unknown Exception : ${t.getMessage}"))
    }

  private[endpoints] def errorLog(msg: String, t: Throwable): Unit = {
    Log.error(msg, t)
  }

  private[endpoints] def errorHttpResponse(msg: String): HttpResponse = {
    val e = HttpEntity(ContentTypes.`application/json`, ByteString(s"""{"jsonrpc":"2.0", "error": {"code": 500, "message": "${msg}"}}"""))
    HttpResponse(StatusCodes.InternalServerError, entity = e)
  }

}