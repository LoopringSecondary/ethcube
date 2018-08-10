package io.upblockchain.worker.client

import scala.concurrent.Future
import io.upblockchain.proto.jsonrpc._
import io.upblockchain.worker.modules.GethClientConfig

trait GethEthereumClient {

  val eth: GethClientConfig

  def handleRequest(req: JsonRPCRequest): Future[JsonRPCResponse]

}