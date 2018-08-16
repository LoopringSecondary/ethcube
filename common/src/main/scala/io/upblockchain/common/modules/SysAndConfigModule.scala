package io.upblockchain.common.modules

import com.google.inject.{ Provides, Singleton }
import com.typesafe.config._
import java.io.File
import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import com.google.inject.Inject
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

    println(s"load configuration file : conf/${env}")

    val config = ConfigFactory.load(env) // .withFallback(ConfigFactory.load())

    println("tcp: ===>>>" + config)

    config
  }

  @Provides @Singleton
  def provideActorSystem(@Inject() config: Config): ActorSystem = ActorSystem("ClusterSystem", config)

}