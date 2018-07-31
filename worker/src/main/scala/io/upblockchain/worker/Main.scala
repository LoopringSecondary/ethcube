package io.upblockchain.worker

import io.upblockchain.common._
import com.google.inject.Guice

import akka.actor.ActorSystem
import akka.actor.Props
import akka.cluster.client.ClusterClientReceptionist
import io.upblockchain.worker.modules.ServiceModule
import akka.stream.ActorMaterializer
import io.upblockchain.worker.client.GethClient

object Main extends App {

  val injector = Guice.createInjector(ServiceModule)
  implicit val system = injector.getInstance(classOf[ActorSystem])
  implicit val mat = injector.getInstance(classOf[ActorMaterializer])

  val serviceA = injector.getActor("GethActor")
  ClusterClientReceptionist(system).registerService(serviceA)

  println(logo)

  lazy val logo = """
   _       __           __            
  | |     / /___  _____/ /_____  _____
  | | /| / / __ \/ ___/ //_/ _ \/ ___/
  | |/ |/ / /_/ / /  / ,< /  __/ /    
  |__/|__/\____/_/  /_/|_|\___/_/     """

}