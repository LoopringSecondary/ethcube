package io.upblockchain.root.rpc

import io.upblockchain.common.json.JsonSupport
import io.upblockchain.root.services.EthJsonRPCService
import akka.stream.ActorMaterializer
import javax.inject.Inject
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import io.upblockchain.common.model.JsonRPCRequestWrapped
import org.json4s.native.JsonMethods.parse
import com.google.inject.{ Provides, Singleton }

@Provides @Singleton
class RootEndpoints @Inject() (service: EthJsonRPCService, mat: ActorMaterializer) extends JsonSupport {

  def apply(): Route = {
    pathEndOrSingleSlash {
      entity(as[JsonRPCRequestWrapped]) { req ⇒
        // complete("Ok")
        println("req ===>>>" + req)

        // complete("Ok")
        onSuccess(service.handleClientRequest(req.toRequest)) { resp ⇒
          complete(parse(resp.json))
        }
      }
    }
  }
}
