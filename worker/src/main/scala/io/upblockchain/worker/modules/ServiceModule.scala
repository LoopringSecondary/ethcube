package io.upblockchain.worker.modules

import io.upblockchain.common.modules.BaseModule
import com.google.inject.{ Provides, Singleton }
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import io.upblockchain.worker.client.GethClient
import akka.stream.ActorMaterializer
import javax.inject.{ Inject, Named }
import akka.actor.Props
import io.upblockchain.worker.services.GethEthereumActor
import akka.actor.ActorRef

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
  def provideMyActor(@Inject() sys: ActorSystem, mat: ActorMaterializer, client: GethClient): ActorRef = {
    sys.actorOf(Props(new GethEthereumActor(client)(sys, mat)))
  }

}

object ServiceModule extends ServiceModule