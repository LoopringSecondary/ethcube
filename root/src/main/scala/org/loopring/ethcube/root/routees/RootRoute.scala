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

package org.loopring.ethcube.root.routees

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
import org.loopring.ethcube.common.json.JsonSupport
import org.loopring.ethcube.root.services.EthJsonRPCService
import org.loopring.ethcube.proto.jsonrpc.JsonRPCRequest

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