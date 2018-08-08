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
import io.upblockchain.worker.services.ClientRouter

trait ServiceModule extends BaseModule {

  override def configure: Unit = {
    bind[GethClient]
  }

  @Provides @Singleton
  def provideActorSystem(@Inject() config: Config): ActorSystem = ActorSystem("ClusterSystem", config)

  @Provides @Singleton
  def provideActorMaterializer(@Inject() sys: ActorSystem): ActorMaterializer = ActorMaterializer()(sys)

  @Provides @Singleton @Named("GethActor")
  def provideGethActor(@Inject() client: GethClient, sys: ActorSystem, mat: ActorMaterializer): Props = {
    Props(new GethEthereumActor(client)(sys, mat))
  }

  @Provides @Singleton @Named("ClientRouter")
  def provideClientRouter(@Inject()@Named("GethActor") actor: Props, sys: ActorSystem, mat: ActorMaterializer): ActorRef = {
    //    Props(new ClientRouter())
    sys.actorOf(Props(new ClientRouter(actor)(sys, mat)), "ClientRouter")
  }

  @Provides @Singleton
  def provideGethIpcConfig(@Inject() config: Config): GethClientConfig = {
    val host = config.getString("geth.host")
    val port = config.getInt("geth.port")
    val ssl = config.getBoolean("geth.ssl")
    val ipcPath = config.getString("geth.ipcpath")

    GethClientConfig(host, port, ssl, ipcPath)
  }

}

object ServiceModule extends ServiceModule

case class GethClientConfig(host: String, port: Int, ssl: Boolean = false, ipcPath: String)
