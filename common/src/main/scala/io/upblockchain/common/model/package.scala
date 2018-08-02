package io.upblockchain.common

package object model {

  case class JsonRPCRequest(jsonrpc: String = "2.0", method: String, params: Any, id: Any)

  case class JsonRPCError(code: Int, message: String, data: String)

  case class JsonRPCResponse(id: Any, jsonrpc: String, result: Option[Any] = None, error: Option[JsonRPCError] = None)

}