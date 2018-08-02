package io.upblockchain.worker.services

import java.io.{BufferedReader, File, InputStreamReader, PrintWriter}

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.marshalling.{Marshal, Marshaller, ToByteStringMarshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes.`text/event-stream`
import akka.http.scaladsl.model.{HttpEntity, RequestEntity}
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import io.upblockchain.common.json.JsonSupport
import io.upblockchain.proto.jsonrpc.JsonRPCRequest
import javax.inject.Inject
import jnr.unixsocket.{UnixSocket, UnixSocketAddress, UnixSocketChannel}
import io.upblockchain.common.model._

import scala.concurrent.Future

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

class GethIpcRoutee(system: ActorSystem, materilizer: ActorMaterializer) extends Actor with JsonSupport {
  import scala.concurrent.ExecutionContext.Implicits.global
  println("#######GethIpcRouteeGethIpcRouteeGethIpcRoutee")
  var address = new UnixSocketAddress(new File("/Users/yuhongyu/myeth_new/data/geth.ipc"))
  private val channel = UnixSocketChannel.open(address)
  var unixSocket = new UnixSocket(channel)
  var w = new PrintWriter(unixSocket.getOutputStream)
  var br = new BufferedReader(new InputStreamReader(unixSocket.getInputStream))
  var requestId = 0
  def receive: Actor.Receive = {
    case req: JsonRPCRequest =>
      requestId += 1
//      var reqJsonStr = Marshal(req).to[]
//      println("reqJsonStrreqJsonStrreqJsonStrreqJsonStr", reqJsonStr)
      //id需要替换 count--id
      var reqJson = """[{"method":"eth_blockNumber","params":[],"id":"aaa","jsonrpc":"2.0"}]"""
      w.println(reqJson)
      w.flush()
      var line = br.readLine()
      sender() ! line
    case _ =>
  }

}

object GethIpcRoutee {
  def props(system: ActorSystem, materilizer: ActorMaterializer) = Props(new GethIpcRoutee(system, materilizer))
}