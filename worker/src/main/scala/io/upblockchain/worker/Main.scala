package io.upblockchain.worker

import java.util.concurrent.TimeUnit

import io.upblockchain.common._
import com.google.inject.Guice
import akka.actor.ActorSystem
import akka.cluster.client.ClusterClientReceptionist
import io.upblockchain.worker.modules.{ ServiceModule, GethClientConfig }
import akka.stream.ActorMaterializer
import akka.util.Timeout
import io.upblockchain.worker.services.StatsMonitor
import scala.reflect.io.Path
import io.upblockchain.common.modules.ConfigModule

object Main extends App {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout(3, TimeUnit.SECONDS)
  val injector = Guice.createInjector(new ConfigModule(args), ServiceModule)
  implicit val system = injector.getInstance(classOf[ActorSystem])
  implicit val mat = injector.getInstance(classOf[ActorMaterializer])

  val gethIpcConfig = injector.getInstance(classOf[GethClientConfig])
  val path: Path = Path(gethIpcConfig.ipcPath)
  if (!path.exists) {
    println(gethIpcConfig.ipcPath, "not exists")
    system.terminate()
  }
  val clientRouter = injector.getActor("ClientRouter")
  ClusterClientReceptionist(system).registerService(clientRouter)

  val monitor = system.actorOf(StatsMonitor.props(clientRouter))
}