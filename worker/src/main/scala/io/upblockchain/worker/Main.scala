package io.upblockchain.worker

import io.upblockchain.common._
import java.util.concurrent.TimeUnit
import io.upblockchain.worker.modules.ServiceModule
import com.google.inject.Guice
import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import io.upblockchain.worker.client.GethEthereumClient
import io.upblockchain.worker.modules.GethClientConfig
import io.upblockchain.common.modules.SysAndConfigModule

object Main extends App {

  //  import scala.concurrent.ExecutionContext.Implicits.global
  //
  //  implicit val timeout = Timeout(3, TimeUnit.SECONDS)

  val injector = Guice.createInjector(new SysAndConfigModule(args), ServiceModule)
  implicit val sys = injector.getInstance(classOf[ActorSystem])
  implicit val mat = injector.getInstance(classOf[ActorMaterializer])

  val actor = injector.getActor("GethActor")
  //
  //  println("actor ===>>" + actor)
  // sys.actorOf(Props(actor), "")

  //  implicit val mat = injector.getInstance(classOf[ActorMaterializer])
  //
  //  val gethIpcConfig = injector.getInstance(classOf[GethClientConfig])
  //  val path: Path = Path(gethIpcConfig.ipcPath)
  //  if (!path.exists) {
  //    println(gethIpcConfig.ipcPath, "not exists")
  //    system.terminate()
  //  }
  //
  //  val clientRouter = injector.getActor("ClientRouter")
  //  ClusterClientReceptionist(system).registerService(clientRouter)

  //  val testRouter = injector.getActor("GethActor")
  //  ClusterClientReceptionist(system).registerService(testRouter)

  // val monitor = system.actorOf(StatsMonitor.props(clientRouter))

  println(logo)

  lazy val logo = """
   _       __           __            
  | |     / /___  _____/ /_____  _____
  | | /| / / __ \/ ___/ //_/ _ \/ ___/
  | |/ |/ / /_/ / /  / ,< /  __/ /    
  |__/|__/\____/_/  /_/|_|\___/_/     """
}