package io.upblockchain.root.modules

import com.google.inject.Provides
import com.google.inject.Singleton
import com.typesafe.config.Config

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.cluster.metrics.AdaptiveLoadBalancingGroup
import akka.cluster.metrics.HeapMetricsSelector
import akka.cluster.routing.ClusterRouterGroup
import akka.cluster.routing.ClusterRouterGroupSettings
import akka.stream.ActorMaterializer
import io.upblockchain.common.modules.BaseModule
import io.upblockchain.root.services.EthJsonRPCService
import javax.inject.Inject
import javax.inject.Named

trait ServiceModule extends BaseModule { self ⇒

  override def configure: Unit = {
    bind[EthJsonRPCService]
  }

  @Provides @Singleton
  def provideActorMaterializer(@Inject() sys: ActorSystem): ActorMaterializer = ActorMaterializer()(sys)

  @Provides @Singleton @Named("ClusterClient")
  def provideClusterPros(@Inject() sys: ActorSystem): ActorRef = {
    // 使用 AdaptiveLoadBalancingGroup 可以达到均衡
    sys.actorOf(ClusterRouterGroup(
      AdaptiveLoadBalancingGroup(HeapMetricsSelector),
      ClusterRouterGroupSettings(
        totalInstances = 100,
        routeesPaths = List("/user/gethActor"),
        allowLocalRoutees = false)).props(), "clusterBalanceGroup")
  }

}

object ServiceModule extends ServiceModule