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
import com.typesafe.config.Config
import io.loopring.ethcube.client.geth.GethHttpEtherClientImpl
import io.loopring.ethcube.client.geth.GethIpcEtherClientImpl

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
  def provideWorkerMonitorActor(
    @Inject() sys: ActorSystem,
    @Named("BroadcastRouter") router1: Router,
    @Named("RoundRobinRouter") router2: Router) = {
    sys.actorOf(Props(classOf[WorkerMonitorActor], router1, router2), "WorkerMonitorActor")
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
  def provideWorkerRoutees(@Inject() sys: ActorSystem, @Named("EtherClientActorRefs") actors: Seq[ActorRef]): Seq[ActorRefRoutee] = {

    actors.zipWithIndex.map {
      case (ac, index) ⇒
        sys.actorOf(Props(classOf[WorkerServiceRoutee], sys, ac), s"Worker-${index}")
    }.map(ActorRefRoutee)

  }

  @Provides @Singleton @Named("EtherClientActorRefs")
  def provideEtherClientActorRefs(@Inject() sys: ActorSystem, mat: ActorMaterializer, config: Config): Seq[ActorRef] = {

    import scala.collection.JavaConverters._

    def provideClientConfig: PartialFunction[Config, ActorRef] = {
      case cfg ⇒
        cfg.getString("ipc_or_http") match {
          case "ipc" ⇒
            val c = EtherClientConfig("", -1, cfg.getString("ipcpath"))
            sys.actorOf(Props(classOf[GethIpcEtherClientImpl], sys, mat, c), cfg.getString("label"))
          case "http" ⇒
            val c = EtherClientConfig(cfg.getString("host"), cfg.getInt("port"), cfg.getString("label"))
            sys.actorOf(Props(classOf[GethHttpEtherClientImpl], sys, mat, c))
          case _ ⇒ throw new Exception("can not match geth ipc or http")
        }
    }

    config.getObjectList("clients").asScala.map(_.toConfig).map(provideClientConfig)
  }

}

object ServicesModule extends ServicesModule

case class EtherClientConfig(host: String, port: Int, ipcPath: String)