package io.loopring.ethcube.model

case class JsonRpcRequest(id: Int, jsonrpc: String = "2.0", method: String, params: Any)

case class JsonRpcError(code: Int, message: String)

case class JsonRpcResponse(id: Option[Any] = None, jsonrpc: String, result: Option[Any] = None, error: Option[JsonRpcError] = None)
