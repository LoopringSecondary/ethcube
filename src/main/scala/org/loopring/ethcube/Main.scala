package org.loopring.ethcube

import akka.actor._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config._
import org.loopring.ethcube.proto.data.EthereumProxySettings
import org.slf4j.LoggerFactory

object Main extends App {

  import collection.JavaConverters._
  lazy val Log = LoggerFactory.getLogger(getClass)

  val config = ConfigFactory.load()

  implicit val system = ActorSystem("ethcube", config)
  implicit val materializer = ActorMaterializer()

  val settings: EthereumProxySettings = {
    EthereumProxySettings(
      config.getInt("pool-size"),
      config.getInt("check-interval-seconds"),
      config.getDouble("healthy-threshold").toFloat,
      config.getConfigList("nodes").asScala map {
        c =>
          EthereumProxySettings.Node(
            c.getString("host"),
            c.getInt("port"),
            c.getString("ipc-path"))
      })
  }

  val ethreumProxy = system.actorOf(
    Props(new EthereumProxy(settings)),
    "ethereum_proxy")

  val host = config.getString("http.host")
  val port = config.getInt("http.port")
  val endpoints = new Endpoints(ethreumProxy)
  Http().bindAndHandle(endpoints.getRoutes, host, port)

  Log.info(s"ethcube started with http service at ${host}:${port}")
}