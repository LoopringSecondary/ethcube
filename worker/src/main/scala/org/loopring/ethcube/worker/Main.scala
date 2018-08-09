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

package org.loopring.ethcube.worker

import java.util.concurrent.TimeUnit

import org.loopring.ethcube.common._
import com.google.inject.Guice
import akka.actor.ActorSystem
import akka.cluster.client.ClusterClientReceptionist
import org.loopring.ethcube.worker.modules.{ GethIpcConfig, ServiceModule }
import akka.stream.ActorMaterializer
import akka.util.Timeout
import org.loopring.ethcube.worker.services.StatsMonitor
import scala.reflect.io.Path

object Main extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout.apply(3, TimeUnit.SECONDS)
  val injector = Guice.createInjector(ServiceModule)
  implicit val system = injector.getInstance(classOf[ActorSystem])
  implicit val mat = injector.getInstance(classOf[ActorMaterializer])

  val gethIpcConfig = injector.getInstance(classOf[GethIpcConfig])
  val path: Path = Path(gethIpcConfig.ipcPath)
  if (!path.exists) {
    println(gethIpcConfig.ipcPath, "not exists")
    system.terminate()
  }
  val clientRouter = injector.getActor("ClientRouter")
  ClusterClientReceptionist(system).registerService(clientRouter)

  val monitor = system.actorOf(StatsMonitor.props(clientRouter))
}