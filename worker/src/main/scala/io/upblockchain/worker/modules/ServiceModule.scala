package io.upblockchain.worker.modules

import io.upblockchain.common.modules.BaseModule
import io.upblockchain.worker.client.GethEthereumClient
import com.google.inject.{ Provides, Singleton }
import com.typesafe.config.Config
import javax.inject.{ Inject, Named }
import akka.actor._
import akka.stream.ActorMaterializer
import io.upblockchain.worker.services.GethEthereumActor
import akka.routing.RandomPool
import io.upblockchain.worker.services.SimpleClusterListener

trait ServiceModule extends BaseModule {

  override def configure: Unit = {
    bind[GethEthereumClient]
  }

  @Provides @Singleton
  def provideActorMaterializer(@Inject() sys: ActorSystem): ActorMaterializer = ActorMaterializer()(sys)

  @Provides @Singleton @Named("GethActor")
  def provideGethActor(@Inject() client: GethEthereumClient, sys: ActorSystem, mat: ActorMaterializer): ActorRef = {
    // 这里定义本地随机路由
    // TODO(Toan) 这里缺少监管策略
    sys.actorOf(RandomPool(5).props(Props(new GethEthereumActor(client)(sys, mat))), "gethActor")
  }

  //  SimpleClusterListener
  @Provides @Singleton @Named("clusterListener")
  def provideClusterListener(@Inject() sys: ActorSystem): ActorRef = {
    sys.actorOf(Props[SimpleClusterListener], "clusterListener")
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
