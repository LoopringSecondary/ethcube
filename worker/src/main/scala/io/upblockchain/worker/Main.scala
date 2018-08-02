package io.upblockchain.worker

import java.util.concurrent.TimeUnit

import io.upblockchain.common._
import com.google.inject.Guice
import akka.actor.ActorSystem
import akka.actor.Props
import akka.cluster.client.ClusterClientReceptionist
import io.upblockchain.worker.modules.ServiceModule
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.google.protobuf.ByteString
import com.google.protobuf.any.Any
import io.upblockchain.common.model.{ JsonRPCRequest, JsonRPCResponse }
import io.upblockchain.worker.services.StatsMonitor

import scala.collection.mutable
import scala.util.Success
//import io.upblockchain.worker.client.GethClient
import akka.pattern.ask

object Main extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout.apply(3, TimeUnit.SECONDS)
  val injector = Guice.createInjector(ServiceModule)
  implicit val system = injector.getInstance(classOf[ActorSystem])
  implicit val mat = injector.getInstance(classOf[ActorMaterializer])

  val clientRouter = injector.getActor("ClientRouter")
  ClusterClientReceptionist(system).registerService(clientRouter)

  val monitor = system.actorOf(StatsMonitor.props(clientRouter))
}