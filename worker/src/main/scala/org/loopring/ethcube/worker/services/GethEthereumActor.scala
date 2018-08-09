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

import akka.actor.ActorSystem
import org.loopring.ethcube.worker.client.GethClient
import akka.stream.ActorMaterializer
import akka.actor.Actor
import akka.http.scaladsl.marshalling.Marshal
import scala.concurrent.duration._
import org.loopring.ethcube.common.json.JsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.pipe
import org.loopring.ethcube.proto.jsonrpc._

class GethEthereumActor(client: GethClient)(implicit system: ActorSystem, mat: ActorMaterializer) extends Actor with JsonSupport {

  import system.dispatcher
  val timeout = 300.millis

  def receive: Actor.Receive = {
    case req: JsonRPCRequest ⇒
      val result = for {
        reqEntity ← Marshal(req).to[RequestEntity]
        httpResp ← client.handleRequest(HttpRequest(method = HttpMethods.POST, entity = reqEntity))
        jsonResp ← Unmarshal(httpResp).to[JsonRPCResponse]
      } yield jsonResp
      result pipeTo sender
    case _ ⇒ context.stop(self)
  }
}