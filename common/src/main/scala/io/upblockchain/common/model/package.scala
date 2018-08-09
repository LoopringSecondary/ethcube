package io.upblockchain.common

import io.upblockchain.proto.jsonrpc._
import org.json4s.native.Serialization._
import org.json4s.native.JsonMethods._
import io.upblockchain.common.json.JsonSupport

package object model extends JsonSupport {

  def toJsonRPCRequestWrapped(req: JsonRPCRequest): JsonRPCRequestWrapped = {
    parse(req.json).extract[JsonRPCRequestWrapped]
  }

  case class JsonRPCRequestWrapped(jsonrpc: String = "2.0", method: String, params: Any, id: Int) {
    def toRequest: JsonRPCRequest = JsonRPCRequest(id = id.toString, json = write(this))
  }

  case class JsonRPCErrorWrapped(code: Int, message: String)

  case class JsonRPCResponseWrapped(id: Option[Any], jsonrpc: String, result: Option[Any] = None, error: Option[JsonRPCErrorWrapped] = None) {
    def toResponse: JsonRPCResponse = JsonRPCResponse(id = id.map(_.toString).getOrElse(""), json = write(this))
  }

}