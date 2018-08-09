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

package org.loopring.ethcube.root.services

import javax.inject.Inject
import akka.actor.ActorSystem
import scala.concurrent.Future
import akka.util.Timeout
import scala.concurrent.duration._
import akka.cluster.client._
import akka.pattern.ask
import javax.inject.Named
import akka.actor.ActorRef
import org.loopring.ethcube.proto.jsonrpc._

class EthJsonRPCService @Inject() (@Named("ClusterClient") cluster: ActorRef) {

  implicit val timeout = Timeout(5 seconds)

  def handleClientRequest(req: JsonRPCRequest): Future[JsonRPCResponse] = {
    // TODO(Toan) 这里返回结果有问题
    // TODO(Toan) 地址可以进行配置化
    (cluster ? ClusterClient.Send("/user/GethActor", req, localAffinity = true)).mapTo[JsonRPCResponse]
  }

}