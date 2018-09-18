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
import org.loopring.lightcone.proto.eth_jsonrpc._
import akka.stream.ActorMaterializer

class LooprEthereumProxyEndpoints(ethereumProxy: ActorRef)(implicit
  system: ActorSystem,
  materializer: ActorMaterializer)
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
    "eth_getBalance" -> ethGetBalanceReq,
    "eth_getTransactionByHash" -> ethGetTransactionByHash,
    "eth_getTransactionReceipt" -> ethGetTransactionReceipt,
    "eth_getBlockWithTxHashByNumber" -> getBlockWithTxHashByNumber,
    "eth_getBlockWithTxObjectByNumber" -> getBlockWithTxObjectByNumber,
    "eth_getBlockWithTxHashByHash" -> getBlockWithTxHashByHash,
    "eth_getBlockWithTxObjectByHash" -> getBlockWithTxObjectByHash,
    "debug_traceTransaction" -> traceTransaction)
  //    "getBalance" -> getBalance,
  //    "getAllowance" -> getAllowance,
  //    "sendRawTransaction" -> sendRawTransaction)

  private def ethGetBalanceReq =
    entity(as[EthGetBalanceReq]) { req ⇒
      val f = (ethereumProxy ? req).mapTo[EthGetBalanceRes]
      complete(f)
    }

  private def ethGetTransactionByHash =
    entity(as[GetTransactionByHashReq]) { req ⇒
      val f = (ethereumProxy ? req).mapTo[GetTransactionByHashRes]
      complete(f)
    }

  private def ethGetTransactionReceipt =
    entity(as[GetTransactionReceiptReq]) { req ⇒
      val f = (ethereumProxy ? req).mapTo[GetTransactionReceiptRes]
      complete(f)
    }

  private def getBlockWithTxHashByNumber =
    entity(as[GetBlockWithTxHashByNumberReq]) { req ⇒
      val f = (ethereumProxy ? req).mapTo[GetBlockWithTxHashByNumberRes]
      complete(f)
    }

  private def getBlockWithTxObjectByNumber =
    entity(as[GetBlockWithTxObjectByNumberReq]) { req ⇒
      val f = (ethereumProxy ? req).mapTo[GetBlockWithTxObjectByNumberRes]
      complete(f)
    }
  private def getBlockWithTxHashByHash =
    entity(as[GetBlockWithTxHashByHashReq]) { req ⇒
      val f = (ethereumProxy ? req).mapTo[GetBlockWithTxHashByHashRes]
      complete(f)
    }
  private def getBlockWithTxObjectByHash =
    entity(as[GetBlockWithTxObjectByHashReq]) { req ⇒
      val f = (ethereumProxy ? req).mapTo[GetBlockWithTxObjectByHashRes]
      complete(f)
    }

  private def traceTransaction =
    entity(as[TraceTransactionReq]) { req ⇒
      val f = (ethereumProxy ? req).mapTo[TraceTransactionRes]
      complete(f)
    }

}