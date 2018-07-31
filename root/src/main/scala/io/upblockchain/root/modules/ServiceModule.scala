package io.upblockchain.root.modules

import io.upblockchain.common.modules.BaseModule
import com.google.inject.{ Provides, Singleton }
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import javax.inject.Inject
import io.upblockchain.root.services.EthJsonRPCService
import akka.actor.ActorRef
import javax.inject.Named
import akka.cluster.client.ClusterClient
import akka.cluster.client.ClusterClientSettings

trait ServiceModule extends BaseModule {

  override def configure: Unit = {
    //    bind[GethClient]
    bind[EthJsonRPCService]
  }

  @Provides @Singleton
  def provideActorSystem: ActorSystem = ActorSystem("RootSystem")

  @Provides @Singleton
  def provideActorMaterializer(@Inject() sys: ActorSystem): ActorMaterializer = ActorMaterializer()(sys)

  @Provides @Singleton @Named("ClusterClient")
  def provideClusterPros(@Inject() sys: ActorSystem): ActorRef = {
    sys.actorOf(ClusterClient.props(ClusterClientSettings(sys)), "cluster_client")
  }

}

object ServiceModule extends ServiceModule