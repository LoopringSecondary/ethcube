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

class RootRoute @Inject() (eth: EthJsonRPCRoute) extends JsonSupport {

  def apply(): Route = {
    index // ~ eth()
  }

  def index: Route = {
    pathEndOrSingleSlash {
      complete(OkResponse("Ok"))
      //{"status: "ok"}
      // val entity = HttpEntity(ContentTypes.`application/json`, ByteString("""{"status: "ok"}"""))
      // complete(HttpResponse(status = StatusCodes.OK, entity = entity))
    }
  }

}

case class OkResponse(status: String = "Ok")