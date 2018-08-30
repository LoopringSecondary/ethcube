package io.loopring.ethcube

import io.loopring.ethcube.common._
import com.google.inject.Guice
import io.loopring.ethcube.common.modules.SysAndConfigModule
import io.loopring.ethcube.modules.ServicesModule
import com.typesafe.config.Config
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import io.loopring.ethcube.endpoints.RootEndpoints
import scala.concurrent.duration._
import io.loopring.ethcube.model.BroadcastRequest

/**
 * entry main function
 */
object Main extends App {

  val injector = Guice.createInjector(new SysAndConfigModule(args), ServicesModule)

  val config = injector.getInstance(classOf[Config])

  implicit val sys = injector.getInstance(classOf[ActorSystem])
  implicit val mat = injector.getInstance(classOf[ActorMaterializer])
  import sys.dispatcher

  // monitor actor
  val receiver = injector.getActor("WorkerMonitorActor")
  sys.scheduler.schedule(initialDelay = 3 seconds, interval = 100 seconds, receiver = receiver, BroadcastRequest)

  // http server
  val r = injector.getInstance(classOf[RootEndpoints])

  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  Http().bindAndHandle(r(), host, port)

  // TODO(Toan)这里还需要添加 websocket

  println(logo)

  lazy val logo = """
      ________  __    ______      __       
     / ____/ /_/ /_  / ____/_  __/ /_  ___ 
    / __/ / __/ __ \/ /   / / / / __ \/ _ \
   / /___/ /_/ / / / /___/ /_/ / /_/ /  __/
  /_____/\__/_/ /_/\____/\__,_/_.___/\___/  """ + s"http://${host}:${port}"

}