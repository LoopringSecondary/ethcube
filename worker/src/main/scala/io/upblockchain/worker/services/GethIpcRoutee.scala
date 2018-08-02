package io.upblockchain.worker.services

import java.io.{ BufferedReader, File, InputStreamReader, PrintWriter }

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.stream.ActorMaterializer
import io.upblockchain.common.json.JsonSupport
import io.upblockchain.common.model.{ JsonRPCRequest, JsonRPCResponse }
import jnr.unixsocket.{ UnixSocket, UnixSocketAddress, UnixSocketChannel }
import org.json4s.jackson.Serialization._

/*

  Copyright 2017 Loopring Project Ltd (Loopring Foundation).

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

*/

class GethIpcRoutee(ipcPath: String)(implicit system: ActorSystem, materilizer: ActorMaterializer) extends Actor with ActorLogging with JsonSupport {
  import context.dispatcher

  val address = new UnixSocketAddress(new File(ipcPath))
  val unixSocket = new UnixSocket(UnixSocketChannel.open(address))
  val w = new PrintWriter(unixSocket.getOutputStream)
  val br = new BufferedReader(new InputStreamReader(unixSocket.getInputStream))
  var requestId = 0

  def receive: Actor.Receive = {
    case req: JsonRPCRequest =>
      requestId = requestId + 1
      val reqJson = write(req.copy(id = requestId + "-" + req.id))
      w.println(reqJson)
      w.flush()
      var line = br.readLine()
      val res = read[JsonRPCResponse](line).copy(id = req.id)
      sender() ! res
    case reqs: List[JsonRPCRequest] =>
      requestId = requestId + 1
      val reqJson = write(reqs.map(req => req.copy(id = requestId + "-" + req.id)))
      w.println(reqJson)
      w.flush()
      var line = br.readLine()
      val res = read[List[JsonRPCResponse]](line)
      sender() ! res.zipWithIndex.map(r => r._1.copy(id = reqs(r._2).id))
    case _ =>
  }

}

object GethIpcRoutee {
  def props(ipcPath: String)(implicit system: ActorSystem, materilizer: ActorMaterializer) = Props(new GethIpcRoutee(ipcPath))
}