package io.loopring.ethcube.common.modules

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import com.typesafe.config.{ Config, ConfigFactory }
import com.google.inject.{ Provides, Singleton }
import akka.actor.ActorSystem
import java.io.File
import scala.io.Source
import java.io.PrintWriter

class SysAndConfigModule(args: Array[String])
  extends AbstractModule with ScalaModule {

  lazy val dockerConfigPath = "/opt/docker/conf"

  lazy val env = {
    val envProperty = System.getProperty("env")
    val envSystem = if (envProperty == null) System.getenv("env") else envProperty
    if (envSystem == null) "dev" else envSystem
  }

  override def configure: Unit = {
    bind[Config].toInstance(provideConfig)
  }

  def provideConfig: Config = {
    val path = ConfigFactory.load.getString(env)
    println("porject loading file => " + path)
    // docker 复制文件到 /opt/docker/conf
    checkDockerEnv
    ConfigFactory parseFile (new File(path)) resolve
  }

  private[modules] def checkDockerEnv(): Unit = {
    if (env == "docker") {

      val docerFile = "docker.conf"
      val docker = new File(s"${dockerConfigPath}/${docerFile}")
      if (!docker.exists()) copy(docerFile)

    }
  }

  private[modules] def copy(fileName: String): Unit = {
    val loader = classOf[SysAndConfigModule].getClassLoader
    if (loader != null) {
      val is = loader.getResourceAsStream(fileName)
      val writer = new PrintWriter(new File(s"${dockerConfigPath}/${fileName}"))
      for (line ← Source.fromInputStream(is).getLines())
        writer.write(line + "\n")

      writer.close()
    }
  }

  @Provides @Singleton
  def provideActorSystem: ActorSystem = ActorSystem("Ethcube")

}