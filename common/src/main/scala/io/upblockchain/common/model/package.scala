package io.upblockchain.common

import io.upblockchain.proto.jsonrpc._
import org.json4s.native.Serialization._
import io.upblockchain.common.json.JsonSupport
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