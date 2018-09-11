/*
 * Copyright 2018 Loopring Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.loopring.ethcube

import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import akka.actor._
import akka.cluster._
import akka.http.scaladsl.Http
import akka.event.Logging
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.model._
import akka.stream._
import akka.pattern.AskTimeoutException
import akka.event.LogSource
import scalapb.json4s.JsonFormat
import scala.concurrent._
import scala.concurrent.duration._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import com.typesafe.config.Config
import org.json4s._
import org.loopring.ethcube.proto.data._

object EthereumProxyEndpoints {
  implicit val logSource: LogSource[AnyRef] = new LogSource[AnyRef] {
    def genString(o: AnyRef): String = o.getClass.getName
    override def getClazz(o: AnyRef): Class[_] = o.getClass
  }
}

class EthereumProxyEndpoints(ethereumProxy: ActorRef)(implicit
  system: ActorSystem,
  materializer: ActorMaterializer)
  extends Json4sSupport {

  implicit val context = system.dispatcher
  implicit val serialization = jackson.Serialization
  implicit val formats = DefaultFormats
  implicit val timeout = Timeout(1 seconds)
  val log = Logging(system, this)

  def getRoutes(): Route = {
    handleExceptions(exceptionHandler)(route)
  }

  private lazy val route = pathEndOrSingleSlash {
    concat(
      post {
        entity(as[JsonRpcReq]) { req =>
          val f = (ethereumProxy ? req).mapTo[JsonRpcRes]
          complete(f)
        }
      })
  } ~ pathPrefix("batch") {
    concat(
      pathEnd {
        concat(
          post {
            entity(as[JsonRpcReqBatch]) { req =>
              val f = (ethereumProxy ? req).mapTo[JsonRpcResBatch]
              complete(f)
            }
          })
      })
  }

  private def exceptionHandler = ExceptionHandler {
    case e: AskTimeoutException =>
      log.error(s"timeout: ${e.getMessage}", e)
      complete(errorResponse("akka ask timeout[5s]"))

    case e: ArithmeticException =>
      extractUri { uri =>
        log.error(s"bad request from : ${uri}", e)
        complete(errorResponse(s"bad request from : ${uri}"))
      }

    case e: StreamTcpException =>
      log.error(s"stream exception : ${e.getMessage}", e)
      complete(errorResponse(s"stream exception : ${e.getMessage}"))

    case t: Throwable =>
      complete(errorResponse(s"Unknown Exception : ${t.getMessage}"))
  }

  private def errorResponse(msg: String): HttpResponse = {
    HttpResponse(
      StatusCodes.InternalServerError,
      entity = HttpEntity(
        ContentTypes.`application/json`,
        s"""{"jsonrpc":"2.0", "error": {"code": 500, "message": "${msg}"}}"""))
  }
}