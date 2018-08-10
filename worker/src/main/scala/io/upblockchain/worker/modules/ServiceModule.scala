package io.upblockchain.worker.modules

import io.upblockchain.common.modules.BaseModule
import io.upblockchain.worker.client.GethEthereumClient
import com.google.inject.{ Provides, Singleton }
import com.typesafe.config.Config
import javax.inject.{ Inject, Named }
import akka.actor._
import akka.stream.ActorMaterializer
import io.upblockchain.worker.services.GethEthereumRouter
import akka.routing.RandomPool
import io.upblockchain.worker.services.SimpleClusterListener
import io.upblockchain.worker.client.geth.GethHttpClientImpl
import io.upblockchain.worker.client.geth.GethIpcClientImpl

trait ServiceModule extends BaseModule {

  override def configure: Unit = {
    // bind[GethEthereumClient]
  }

  @Provides @Singleton
  def provideActorMaterializer(@Inject() sys: ActorSystem): ActorMaterializer = ActorMaterializer()(sys)

  @Provides @Singleton
  def provideGethEthereumClient(@Inject() sys: ActorSystem, mat: ActorMaterializer, eth: GethClientConfig): GethEthereumClient = {

    eth.ipcPath match {
      case s if s.isEmpty() ⇒ new GethHttpClientImpl(sys, mat, eth)
      case _ ⇒ new GethIpcClientImpl(eth)
    }

  }

  @Provides @Singleton @Named("GethActor")
  def provideGethActor(@Inject() client: GethEthereumClient, sys: ActorSystem, mat: ActorMaterializer): ActorRef = {
    // 这里定义本地随机路由
    // TODO(Toan) 这里缺少监管策略
    sys.actorOf(RandomPool(5).props(Props(new GethEthereumRouter(client)(sys, mat))), "gethActor")
  }

  //  SimpleClusterListener
  @Provides @Singleton @Named("clusterListener")
  def provideClusterListener(@Inject() sys: ActorSystem): ActorRef = {
    sys.actorOf(Props[SimpleClusterListener], "clusterListener")
  }

  @Provides @Singleton
  def provideGethIpcConfig(@Inject() config: Config): GethClientConfig = {

    config.getString("geth.ipc_or_http") match {
      case s if "ipc".equalsIgnoreCase(s) ⇒
        val ipcPath = config.getString("geth.ipcpath")
        GethClientConfig("", 0, false, ipcPath)
      case s if "http".equalsIgnoreCase(s) ⇒
        val host = config.getString("geth.host")
        val port = config.getInt("geth.port")
        val ssl = config.getBoolean("geth.ssl")
        GethClientConfig(host, port, ssl, "")
      case _ ⇒ throw new Exception("can not match geth ipc or http")
    }
  }

}

object ServiceModule extends ServiceModule

case class GethClientConfig(host: String, port: Int, ssl: Boolean = false, ipcPath: String)
