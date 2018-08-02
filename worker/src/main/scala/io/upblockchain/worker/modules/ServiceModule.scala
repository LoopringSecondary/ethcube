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
import io.upblockchain.worker.services.{ ClientRouter, GethEthereumActor, SimpleClusterListener }
import akka.actor.ActorRef
import akka.util.Timeout

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
  def provideGethActor(@Inject() sys: ActorSystem, mat: ActorMaterializer, client: GethClient): ActorRef = {
    sys.actorOf(Props(new GethEthereumActor(client)(sys, mat)), "GethActor")
  }

  @Provides @Singleton @Named("ClientRouter")
  def provideClientRouter(implicit sys: ActorSystem, mat: ActorMaterializer): ActorRef = {
    sys.actorOf(Props(new ClientRouter("/Users/yuhongyu/myeth_new/data/geth.ipc")(sys, mat)), "ClientRouter")
  }

  //  @Provides @Singleton @Named("ClusterListener")
  //  def provideClusterListener(@Inject() sys: ActorSystem): ActorRef = {
  //    sys.actorOf(Props[SimpleClusterListener], "ClusterListener")
  //  }

  @Provides @Singleton
  def provideEthClientConfig(@Inject() config: Config): EthClientConfig = {
    EthClientConfig(config.getString("eth.host"), config.getInt("eth.port"), config.getBoolean("eth.ssl"))
  }

}

object ServiceModule extends ServiceModule

case class EthClientConfig(host: String, port: Int, ssl: Boolean = false)