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

import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.model._
import akka.pattern.AskTimeoutException
import scalapb.json4s.JsonFormat
import scala.concurrent._
import scala.concurrent.duration._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import com.typesafe.config.Config
import org.json4s._
import org.loopring.ethcube.proto.data._
import org.loopring.ethcube.proto.eth_jsonrpc._
import akka.stream.ActorMaterializer

class LooprEthereumProxyEndpoints(ethereumProxy: ActorRef)(implicit
    system: ActorSystem,
    materializer: ActorMaterializer
)
  extends Json4sSupport {

  implicit val context = system.dispatcher
  implicit val serialization = jackson.Serialization
  implicit val formats = DefaultFormats
  implicit val timeout = Timeout(3 seconds)

  lazy val routes: Route = ctx ⇒ {
    val listing = etherRoutingMap.map {
      case (segment, route) ⇒
        path(segment) {
          post { route }
        }
    }
    concat(listing.toList: _*)(ctx)
  }

  private val etherRoutingMap = Map[String, RequestContext ⇒ Future[RouteResult]](
    "eth_blockNumber" -> ethBlockNumber,
    "eth_getBalance" -> routeContext[EthGetBalanceReq, EthGetBalanceRes],
    "eth_getTransactionByHash" -> routeContext[GetTransactionByHashReq, GetTransactionByHashRes],
    "eth_getTransactionReceipt" -> routeContext[GetTransactionReceiptReq, GetTransactionReceiptRes],
    "eth_getBlockWithTxHashByNumber" -> routeContext[GetBlockWithTxHashByNumberReq, GetBlockWithTxHashByNumberRes],
    "eth_getBlockWithTxObjectByNumber" -> routeContext[GetBlockWithTxObjectByNumberReq, GetBlockWithTxObjectByNumberRes],
    "eth_getBlockWithTxHashByHash" -> routeContext[GetBlockWithTxHashByHashReq, GetBlockWithTxHashByHashRes],
    "eth_getBlockWithTxObjectByHash" -> routeContext[GetBlockWithTxObjectByHashReq, GetBlockWithTxObjectByHashRes],
    "debug_traceTransaction" -> routeContext[TraceTransactionReq, TraceTransactionRes],
    "eth_sendRawTransaction" -> routeContext[SendRawTransactionReq, SendRawTransactionRes],
    "eth_getTransactionCount" -> routeContext[GetNonceReq, GetNonceRes],
    "eth_getBlockTransactionCountByHash" -> routeContext[GetBlockTransactionCountReq, GetBlockTransactionCountRes],
    "eth_call" -> routeContext[EthCallReq, EthCallRes]
  )

  private def ethBlockNumber = {
    val f = (ethereumProxy ? EthBlockNumberReq()).mapTo[EthBlockNumberRes]
    complete(f)
  }

  private def routeContext[P: Manifest, T <: ProtoBuf[_]: Manifest] = {
    entity(as[P]) { req ⇒
      // 直接mapTo不会自动转json
      val f = (ethereumProxy ? req).mapTo[T].map(toResponse)
      complete(f)
    }
  }

  private def toResponse(t: ProtoBuf[_]): HttpResponse = {
    HttpResponse(
      StatusCodes.OK,
      entity = HttpEntity(
        ContentTypes.`application/json`,
        JsonFormat.toJsonString(t)
      )
    )
  }

}
