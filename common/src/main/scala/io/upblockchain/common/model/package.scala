package io.upblockchain.common

package object model {

  case class JsonRPCRequest(jsonrpc: String = "2.0", method: String, params: Any, id: Int)

  case class JsonRPCError(code: Int, message: String)

  case class JsonRPCResponse(id: Int, jsonrpc: String, result: Option[Any] = None, error: Option[JsonRPCError] = None)

}