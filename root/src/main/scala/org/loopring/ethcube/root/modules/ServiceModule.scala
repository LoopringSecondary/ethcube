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

package org.loopring.ethcube.root.modules

import org.loopring.ethcube.common.modules.BaseModule
import com.google.inject.{ Provides, Singleton }
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import javax.inject.Inject
import org.loopring.ethcube.root.services.EthJsonRPCService
import akka.actor.ActorRef
import javax.inject.Named
import akka.cluster.client.ClusterClient
import akka.cluster.client.ClusterClientSettings
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

trait ServiceModule extends BaseModule {

  override def configure: Unit = {
    bind[EthJsonRPCService]
  }

  @Provides @Singleton
  def provideConfig: Config = ConfigFactory load

  @Provides @Singleton
  def provideActorSystem: ActorSystem = ActorSystem("RootSystem")

  @Provides @Singleton
  def provideActorMaterializer(@Inject() sys: ActorSystem): ActorMaterializer = ActorMaterializer()(sys)

  @Provides @Singleton @Named("ClusterClient")
  def provideClusterPros(@Inject() sys: ActorSystem): ActorRef = {
    sys.actorOf(ClusterClient.props(ClusterClientSettings(sys)), "cluster_client")
  }

  //  @Provides @Singleton @Named("SimpleClusterListener")
  //  def provideClusterListener(@Inject() sys: ActorSystem): ActorRef = {
  //    sys.actorOf(Props[SimpleClusterListener], "SimpleClusterListener")
  //  }

}

object ServiceModule extends ServiceModule