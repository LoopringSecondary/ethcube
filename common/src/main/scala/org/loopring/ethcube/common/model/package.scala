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

package org.loopring.ethcube.common

import org.loopring.ethcube.proto.jsonrpc._
import org.json4s.native.Serialization._
import org.loopring.ethcube.common.json.JsonSupport
import org.json4s.native.JsonMethods._

package object model extends JsonSupport {

  def toJsonRPCRequestWrapped(req: JsonRPCRequest): JsonRPCRequestWrapped = {
    parse(req.req).extract[JsonRPCRequestWrapped]
  }

  case class JsonRPCRequestWrapped(jsonrpc: String = "2.0", method: String, params: Any, id: Any) {

    def toRequest: JsonRPCRequest = {
      JsonRPCRequest(write(this))
    }
  }

  case class JsonRPCErrorWrapped(code: Int, message: String, data: String)

  case class JsonRPCResponseWrapped(id: Any, jsonrpc: String, result: Option[Any] = None, error: Option[JsonRPCErrorWrapped] = None) {

    def toResponse: JsonRPCResponse = JsonRPCResponse(write(this))

  }

}