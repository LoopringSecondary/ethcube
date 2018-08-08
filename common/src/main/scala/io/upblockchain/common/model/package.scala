package io.upblockchain.common

import io.upblockchain.proto.jsonrpc._
import org.json4s.native.Serialization._
import org.json4s.native.JsonMethods._
import io.upblockchain.common.json.JsonSupport

package object model extends JsonSupport {

  def toJsonRPCRequestWrapped(req: JsonRPCRequest): JsonRPCRequestWrapped = {
    parse(req.json).extract[JsonRPCRequestWrapped]
  }

  case class JsonRPCRequestWrapped(jsonrpc: String = "2.0", method: String, params: Any, id: Option[Any]) {

    def toRequest: JsonRPCRequest = {
      JsonRPCRequest(write(this))
    }
  }

  case class JsonRPCErrorWrapped(code: Int, message: String)

  case class JsonRPCResponseWrapped(id: Option[Any], jsonrpc: String, result: Option[Any] = None, error: Option[JsonRPCErrorWrapped] = None) {
    def toResponse: JsonRPCResponse = JsonRPCResponse(write(this))
  }

}