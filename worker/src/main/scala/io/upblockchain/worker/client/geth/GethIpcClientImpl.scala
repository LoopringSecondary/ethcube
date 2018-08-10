package io.upblockchain.worker.client.geth

import io.upblockchain.worker.client.GethEthereumClient
import io.upblockchain.proto.jsonrpc._
import scala.concurrent.Future
import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.upblockchain.worker.modules.GethClientConfig
import jnr.unixsocket.UnixSocketAddress
import java.io.File
import jnr.unixsocket.UnixSocketChannel
import java.io.PrintWriter
import java.nio.channels.Channels
import java.io.InputStreamReader
import io.upblockchain.common.json.JsonSupport
import java.nio.CharBuffer
import org.json4s.native.Serialization.{ read }
import io.upblockchain.common.model.JsonRPCResponseWrapped
import org.slf4j.LoggerFactory

class GethIpcClientImpl @Inject() (val eth: GethClientConfig) extends GethEthereumClient with JsonSupport {

  lazy val Log = LoggerFactory.getLogger(getClass)
  val address = new UnixSocketAddress(new File(eth.ipcPath))
  val channel = UnixSocketChannel.open(address)

  val writer = new PrintWriter(Channels.newOutputStream(channel))
  val reader = new InputStreamReader(Channels.newInputStream(channel))

  def handleRequest(req: JsonRPCRequest): Future[JsonRPCResponse] = {

    writer.print(req.json)
    writer.flush()

    val result = CharBuffer.allocate(1024)
    reader.read(result)
    result.flip()

    Log.info(s"geth ipc client json response => ${result.toString}")

    val wrapped = read[JsonRPCResponseWrapped](result.toString())

    Future.successful(wrapped.toResponse)
  }

}