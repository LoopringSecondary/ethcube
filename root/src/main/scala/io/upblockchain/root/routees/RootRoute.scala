package io.upblockchain.root.routees

import scala.concurrent.duration.DurationInt

import org.json4s.native.JsonMethods.parse

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import io.upblockchain.common.model.JsonRPCRequestWrapped
import io.upblockchain.root.services.EthJsonRPCService
import javax.inject.Inject
import io.upblockchain.common.json.JsonSupport

class RootRoute @Inject() (service: EthJsonRPCService, mat: ActorMaterializer) extends JsonSupport {

  val timeout = 300.millis

  implicit val d = mat

  def apply(): Route = {
    pathEndOrSingleSlash {
      entity(as[JsonRPCRequestWrapped]) { req ⇒
        onSuccess(service.handleClientRequest(req.toRequest)) { resp ⇒
          complete(parse(resp.json))
        }
      }
    }
  }
}