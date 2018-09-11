package org.loopring.ethcube

import akka.actor._
import akka.routing._
import scalapb.json4s.JsonFormat
import jnr.unixsocket._
import java.io._
import java.nio.channels.Channels
import java.nio.CharBuffer
import org.loopring.ethcube.proto.data._

private class IpcConnector(node: EthereumProxySettings.Node)
  extends Actor
  with ActorLogging {

  import context.dispatcher

  val address = new UnixSocketAddress(new File(node.ipcPath))
  val channel = UnixSocketChannel.open(address)

  val writer = new PrintWriter(Channels.newOutputStream(channel))
  val reader = new InputStreamReader(Channels.newInputStream(channel))

  def receive: Receive = {
    case req: JsonRpcReq =>
      try {
        writer.print(JsonFormat.toJsonString(req))
        writer.flush()

        val result = CharBuffer.allocate(1024)
        reader.read(result)
        result.flip()
        log.debug(s"ipc response raw: ${result}")

        val response = JsonFormat.fromJsonString[JsonRpcRes](result.toString)
        log.debug(s"ipx response object: ${response}")
        sender ! response
      } catch {
        case e: Throwable => log.error(e.getMessage)
      }
  }
}