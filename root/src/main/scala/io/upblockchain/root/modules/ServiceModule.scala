package io.upblockchain.root.modules

import io.upblockchain.common.modules.BaseModule
import com.google.inject.{ Provides, Singleton }
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer
import javax.inject.Inject
import io.upblockchain.root.services.EthJsonRPCService
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
  def provideActorSystem(@Inject() config: Config): ActorSystem = ActorSystem("ClusterSystem", config)

  @Provides @Singleton
  def provideActorMaterializer(@Inject() sys: ActorSystem): ActorMaterializer = ActorMaterializer()(sys)

  @Provides @Singleton @Named("ClusterClient")
  def provideClusterPros(@Inject() sys: ActorSystem): ActorRef = {
    sys.actorOf(ClusterClient.props(ClusterClientSettings(sys)), "cluster_client")
  }

}

object ServiceModule extends ServiceModule