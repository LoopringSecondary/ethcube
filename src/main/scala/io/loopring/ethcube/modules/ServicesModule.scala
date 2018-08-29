package io.loopring.ethcube.modules

import io.loopring.ethcube.common.modules.BaseModule
import com.google.inject.{ Provides, Singleton }
import com.google.inject.Inject
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props
import javax.inject.Named
import io.loopring.ethcube.services.WorkerServiceRoutee
import akka.routing.Router
import akka.routing.RoundRobinRoutingLogic
import akka.routing.ActorRefRoutee
import scala.collection.immutable
import io.loopring.ethcube.services.WorkerRoundRobinActor
import akka.routing.BroadcastRoutingLogic
import io.loopring.ethcube.services.WorkerMonitorActor

trait ServicesModule extends BaseModule { self ⇒

  override def configure: Unit = {
  }

  @Provides @Singleton
  def provideActorMaterializer(@Inject() sys: ActorSystem): ActorMaterializer = ActorMaterializer()(sys)

  @Provides @Singleton @Named("WorkerRoundRobinActor")
  def provideWorkerRoundRobinActor(@Inject() sys: ActorSystem, @Named("RoundRobinRouter") router: Router) = {
    sys.actorOf(Props(classOf[WorkerRoundRobinActor], router), "WorkerRoundRobinActor")
  }

  @Provides @Singleton @Named("WorkerMonitorActor")
  def provideWorkerMonitorActor(@Inject() sys: ActorSystem, @Named("BroadcastRouter") router: Router) = {
    sys.actorOf(Props(classOf[WorkerMonitorActor], router), "WorkerMonitorActor")
  }

  @Provides @Singleton @Named("RoundRobinRouter")
  def provideRoundRobinRouter(@Inject()@Named("WorkerRoutees") routees: Seq[ActorRefRoutee]): Router = {
    Router(RoundRobinRoutingLogic(), immutable.IndexedSeq(routees: _*))
  }

  @Provides @Singleton @Named("BroadcastRouter")
  def provideBroadcastRouter(@Inject()@Named("WorkerRoutees") routees: Seq[ActorRefRoutee]): Router = {
    Router(BroadcastRoutingLogic(), immutable.IndexedSeq(routees: _*))
  }

  @Provides @Singleton @Named("WorkerRoutees")
  def provideWorkerRoutees(@Inject() sys: ActorSystem): Seq[ActorRefRoutee] = {
    Seq.range(1, 5).map { index ⇒
      sys.actorOf(Props[WorkerServiceRoutee], s"Worker-${index}")
    }.map(ActorRefRoutee)
  }

}

object ServicesModule extends ServicesModule