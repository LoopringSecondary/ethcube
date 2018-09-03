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
import org.slf4j.LoggerFactory
import akka.http.scaladsl.server.Route
import io.loopring.ethcube.endpoints.LooprEndpoints
import akka.http.scaladsl.server.Directives._

/**
 * entry main function
 */
object Main extends App {

  lazy val Log = LoggerFactory.getLogger(getClass)

  val injector = Guice.createInjector(new SysAndConfigModule(args), ServicesModule)

  val config = injector.getInstance(classOf[Config])

  implicit val sys = injector.getInstance(classOf[ActorSystem])
  implicit val mat = injector.getInstance(classOf[ActorMaterializer])
  import sys.dispatcher

  // monitor actor
  val receiver = injector.getActor("WorkerControlerActor")
  val initial = config.getInt("schedule.initial") seconds
  val interval = config.getInt("schedule.interval") seconds

  Log.info(s"worker monitor schedule { initial: ${initial}, interval: ${interval} }")
  sys.scheduler.schedule(initialDelay = initial, interval = interval, receiver = receiver, BroadcastRequest)

  // http server
  val r = injector.getInstance(classOf[RootEndpoints])
  val l = injector.getInstance(classOf[LooprEndpoints])

  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  Http().bindAndHandle(r() ~ l(), host, port)

  // TODO(Toan)这里还需要添加 websocket

  println(logo)

  lazy val logo = """
      ________  __    ______      __       
     / ____/ /_/ /_  / ____/_  __/ /_  ___ 
    / __/ / __/ __ \/ /   / / / / __ \/ _ \
   / /___/ /_/ / / / /___/ /_/ / /_/ /  __/
  /_____/\__/_/ /_/\____/\__,_/_.___/\___/  """ + s"http://${host}:${port}"

}