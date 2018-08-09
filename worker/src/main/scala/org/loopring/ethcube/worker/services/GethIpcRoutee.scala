/*
 * Copyright 2018 Loopring Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.loopring.ethcube.worker.services

import org.loopring.ethcube.common.model._
import java.io.{ BufferedReader, File, InputStreamReader, PrintWriter }
import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.stream.ActorMaterializer
import org.loopring.ethcube.common.json.JsonSupport
import jnr.unixsocket.{ UnixSocket, UnixSocketAddress, UnixSocketChannel }
import org.json4s.jackson.Serialization._
import org.loopring.ethcube.proto.jsonrpc._

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

  val requestId = new java.util.concurrent.atomic.AtomicInteger

  def receive: Actor.Receive = {
    case req: JsonRPCRequest ⇒

      val reqWrapped = toJsonRPCRequestWrapped(req)
      val reqJson = write(reqWrapped.copy(id = requestId.getAndIncrement + "-" + reqWrapped.id))
      w.println(reqJson)
      w.flush()
      var line = br.readLine()
      val res = read[JsonRPCResponseWrapped](line).copy(id = reqWrapped.id)
      sender() ! res.toResponse
    case reqs: JsonRPCRequestSeq ⇒

      val reqSeqWrapped = reqs.req.seq.map(toJsonRPCRequestWrapped)
      val reqJson = write(reqSeqWrapped.map(req ⇒ req.copy(id = requestId.getAndIncrement + "-" + req.id)))
      w.println(reqJson)
      w.flush()

      var line = br.readLine()
      val res = read[List[JsonRPCResponseWrapped]](line)
      sender() ! res.zipWithIndex.map {
        case (r, index) ⇒
          r.copy(id = reqSeqWrapped(index).id)
      }
    case _ ⇒
  }

}

object GethIpcRoutee {
  def props(ipcPath: String)(implicit system: ActorSystem, materilizer: ActorMaterializer) = Props(new GethIpcRoutee(ipcPath))
}