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
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import akka.cluster.routing.ClusterRouterGroup
import akka.routing.RoundRobinGroup
import akka.cluster.routing.ClusterRouterGroupSettings

trait ServiceModule extends BaseModule { self ⇒

  override def configure: Unit = {
    bind[EthJsonRPCService]
  }

  @Provides @Singleton
  def provideActorSystem(@Inject() config: Config): ActorSystem = ActorSystem("ClusterSystem", config)

  @Provides @Singleton
  def provideActorMaterializer(@Inject() sys: ActorSystem): ActorMaterializer = ActorMaterializer()(sys)

  @Provides @Singleton @Named("ClusterClient")
  def provideClusterPros(@Inject() sys: ActorSystem): ActorRef = {

    sys.actorOf(
      ClusterRouterGroup(
        RoundRobinGroup(Nil),
        ClusterRouterGroupSettings(
          totalInstances = 100,
          routeesPaths = List("/user/GethActor"),
          allowLocalRoutees = false)).props(),
      name = "workerRouter")

    // sys.actorOf(ClusterClient.props(ClusterClientSettings(sys)), "ClusterClient")
  }

}

object ServiceModule extends ServiceModule