package io.upblockchain.worker

import io.upblockchain.common._
import java.util.concurrent.TimeUnit
import io.upblockchain.common.modules.ConfigModule
import io.upblockchain.worker.modules.ServiceModule
import com.google.inject.Guice
import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import io.upblockchain.worker.client.GethEthereumClient
import io.upblockchain.worker.modules.GethClientConfig
import io.upblockchain.worker.services.GethClientActor

object Main extends App {

  val config = ConfigFactory load

  implicit val as = ActorSystem("ClusterSystem", config)
  implicit val am = ActorMaterializer()
  implicit val ec = as.dispatcher

  val d = GethClientConfig("127.0.0.1", 8545, false, "")

  val client = new GethEthereumClient(as, am, d)

  //  val ss = new GethClientActor(client)(as, am)
  //
  as.actorOf(Props(new GethClientActor(client)), "GethActor")

  //  import akka.cluster.routing.ClusterRouterGroup.apply
  //  import scala.concurrent.ExecutionContext.Implicits.global
  //
  //  implicit val timeout = Timeout(3, TimeUnit.SECONDS)
  //  val injector = Guice.createInjector(new ConfigModule(args), ServiceModule)
  //  implicit val sys = injector.getInstance(classOf[ActorSystem])
  //  val actor = injector.getActor("GethActor")
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