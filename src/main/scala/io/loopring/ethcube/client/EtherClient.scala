package io.loopring.ethcube.client

import io.loopring.ethcube.modules.EtherClientConfig
import scala.concurrent.Future
import io.loopring.ethcube.model.JsonRPCResponse
import io.loopring.ethcube.model.JsonRpcRequest

trait EtherClient {

  val config: EtherClientConfig

}