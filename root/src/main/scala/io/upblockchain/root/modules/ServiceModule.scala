package io.upblockchain.root.modules

import com.google.inject.{ Provides, Singleton }

import akka.actor.{ ActorRef, ActorSystem }
import akka.cluster.metrics.{ AdaptiveLoadBalancingGroup, HeapMetricsSelector }
import akka.cluster.routing.{ ClusterRouterGroup, ClusterRouterGroupSettings }
import akka.stream.ActorMaterializer
import io.upblockchain.common.modules.BaseModule
import javax.inject.{ Inject, Named }
import io.upblockchain.root.cluster.SimpleClusterListener
import akka.actor.Props
import akka.cluster.Cluster
import akka.routing.Router
import io.upblockchain.root.routing.SimpleRoutingLogic

trait ServiceModule extends BaseModule { self ⇒

  override def configure: Unit = {
    bind[SimpleRoutingLogic]
  }

  @Provides @Singleton
  def provideActorMaterializer(@Inject() sys: ActorSystem): ActorMaterializer = ActorMaterializer()(sys)

  @Provides @Singleton
  def provideCluster(@Inject() sys: ActorSystem): Cluster = Cluster(sys)

  @Provides @Singleton @Named("ClusterClient")
  def provideClusterPros(@Inject() sys: ActorSystem): ActorRef = {
    // 使用 AdaptiveLoadBalancingGroup 可以达到均衡
    // val a = Router(logic, routees)
    sys.actorOf(ClusterRouterGroup(
      AdaptiveLoadBalancingGroup(HeapMetricsSelector),
      ClusterRouterGroupSettings(
        totalInstances = 100,
        routeesPaths = List("/user/gethActor"),
        allowLocalRoutees = false)).props(), "clusterBalanceGroup")
  }

  //  @Provides @Singleton @Named("clusterListener")
  //  def provideClusterListener(@Inject() sys: ActorSystem, cluster: Cluster, logic: SimpleRoutingLogic): ActorRef = {
  //    sys.actorOf(Props(classOf[SimpleClusterListener], cluster, logic), "clusterListener")
  //  }

}

object ServiceModule extends ServiceModule