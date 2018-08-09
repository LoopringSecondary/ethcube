package io.upblockchain.root.modules

import com.google.inject.{ Provides, Singleton }

import akka.actor.{ ActorRef, ActorSystem }
import akka.cluster.metrics.{ AdaptiveLoadBalancingGroup, HeapMetricsSelector }
import akka.cluster.routing.{ ClusterRouterGroup, ClusterRouterGroupSettings }
import akka.stream.ActorMaterializer
import io.upblockchain.common.modules.BaseModule
import javax.inject.{ Inject, Named }

trait ServiceModule extends BaseModule { self ⇒

  override def configure: Unit = {}

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