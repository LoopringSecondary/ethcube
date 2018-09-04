package io.loopring.ethcube.client.geth

import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.loopring.ethcube.modules.EtherClientConfig
import io.loopring.ethcube.client.EtherClient
import akka.actor.Actor
import org.slf4j.LoggerFactory
import io.loopring.ethcube.model.JsonRpcRequest
import jnr.unixsocket.UnixSocketAddress
import jnr.unixsocket.UnixSocketChannel
import java.io.PrintWriter
import java.io.InputStreamReader
import java.io.File
import java.nio.channels.Channels
import org.json4s.native.Serialization._
import io.loopring.ethcube.common.json.JsonSupport
import java.nio.CharBuffer
import io.loopring.ethcube.model.JsonRpcResponse

class GethIpcEtherClientImpl @Inject() (sys: ActorSystem, mat: ActorMaterializer, val config: EtherClientConfig) extends EtherClient with Actor with JsonSupport {

  lazy val Log = LoggerFactory.getLogger(getClass)

  val address = new UnixSocketAddress(new File(config.ipcPath))
  val channel = UnixSocketChannel.open(address)

  val writer = new PrintWriter(Channels.newOutputStream(channel))
  val reader = new InputStreamReader(Channels.newInputStream(channel))

  def receive: Actor.Receive = {
    case req: JsonRpcRequest ⇒
      writer.print(write(req))
      writer.flush()

      val result = CharBuffer.allocate(1024)
      reader.read(result)
      result.flip()

      Log.info(s"geth ipc client json response => ${result.toString}")

      val response = read[JsonRpcResponse](result.toString())

      Log.info(s"json parse => ${response}")

      sender ! response
  }

}