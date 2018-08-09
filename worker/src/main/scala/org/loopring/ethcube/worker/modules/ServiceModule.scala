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

package org.loopring.ethcube.worker.modules

import org.loopring.ethcube.common.modules.BaseModule
import com.google.inject.{ Provides, Singleton }
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import org.loopring.ethcube.worker.client.GethClient
import akka.stream.ActorMaterializer
import javax.inject.{ Inject, Named }
import akka.actor.Props
import org.loopring.ethcube.worker.services.{ ClientRouter, GethEthereumActor, SimpleClusterListener }
import akka.actor.ActorRef
import akka.util.Timeout

trait ServiceModule extends BaseModule {

  override def configure: Unit = {
    bind[GethClient]
  }

  @Provides @Singleton
  def provideConfig: Config = ConfigFactory load

  @Provides @Singleton
  def provideActorSystem(@Inject() config: Config): ActorSystem = ActorSystem("ClusterSystem", config)

  @Provides @Singleton
  def provideActorMaterializer(@Inject() sys: ActorSystem): ActorMaterializer = ActorMaterializer()(sys)

  @Provides @Singleton @Named("GethActor")
  def provideGethActor(@Inject() sys: ActorSystem, mat: ActorMaterializer, client: GethClient): ActorRef = {
    sys.actorOf(Props(new GethEthereumActor(client)(sys, mat)), "GethActor")
  }

  @Provides @Singleton @Named("ClientRouter")
  def provideClientRouter(@Inject() gethConfig: GethIpcConfig, sys: ActorSystem, mat: ActorMaterializer): ActorRef = {
    sys.actorOf(Props(new ClientRouter(gethConfig.ipcPath)(sys, mat)), "ClientRouter")
  }

  //  @Provides @Singleton @Named("ClusterListener")
  //  def provideClusterListener(@Inject() sys: ActorSystem): ActorRef = {
  //    sys.actorOf(Props[SimpleClusterListener], "ClusterListener")
  //  }

  @Provides @Singleton
  def provideEthClientConfig(@Inject() config: Config): EthClientConfig = {
    EthClientConfig(config.getString("eth.host"), config.getInt("eth.port"), config.getBoolean("eth.ssl"))
  }

  @Provides @Singleton
  def provideGethIpcConfig(@Inject() config: Config): GethIpcConfig = {
    GethIpcConfig(config.getString("geth.ipcpath"))
  }

}

object ServiceModule extends ServiceModule

case class EthClientConfig(host: String, port: Int, ssl: Boolean = false)

case class GethIpcConfig(ipcPath: String)
