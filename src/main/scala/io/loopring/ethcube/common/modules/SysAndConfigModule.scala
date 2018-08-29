package io.loopring.ethcube.common.modules

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import com.typesafe.config.{ Config, ConfigFactory }
import com.google.inject.{ Provides, Singleton }
import akka.actor.ActorSystem

class SysAndConfigModule(args: Array[String]) extends AbstractModule with ScalaModule {

  override def configure: Unit = {
    bind[Config].toInstance(provideConfig)
  }

  def provideConfig: Config = {

    val env = {
      val envProperty = System.getProperty("env")
      val envSystem = if (envProperty == null) System.getenv("env") else envProperty
      val envArgs = if (envSystem == null) { if (args.isEmpty) null else args(0) } else envSystem
      if (envArgs == null) "dev" else envArgs
    }

    println(s"load configuration file : ${env}")

    ConfigFactory.load(env)

  }

  @Provides @Singleton
  def provideActorSystem: ActorSystem = ActorSystem("EthereumCubeSystem")

}