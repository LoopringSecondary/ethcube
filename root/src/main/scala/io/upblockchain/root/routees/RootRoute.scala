package io.upblockchain.root.routees

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import javax.inject.Inject
import akka.actor.Actor
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.HttpEntity
import akka.util.ByteString
import io.upblockchain.common.json.JsonSupport
import io.upblockchain.root.services.EthJsonRPCService
import io.upblockchain.proto.jsonrpc.JsonRPCRequest

class RootRoute @Inject() (service: EthJsonRPCService) extends JsonSupport {

  def apply(): Route = {
    pathEndOrSingleSlash {
      entity(as[JsonRPCRequest]) { req ⇒
        onSuccess(service.handleClientRequest(req)) { resp ⇒
          // TODO(Toan) 这里应该做 application/json 处理 还没做测试
          complete(HttpEntity(ContentTypes.`application/json`, resp.resp))
        }
      }
    }
  }
}