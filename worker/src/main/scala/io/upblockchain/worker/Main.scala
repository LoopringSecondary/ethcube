package io.upblockchain.worker

import io.upblockchain.common._
import com.google.inject.Guice
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.upblockchain.common.ActorInjector
import io.upblockchain.common.modules.SysAndConfigModule
import io.upblockchain.worker.modules.ServiceModule

object Main extends App {

  val injector = Guice.createInjector(new SysAndConfigModule(args), ServiceModule)
  implicit val sys = injector.getInstance(classOf[ActorSystem])

  val actor = injector.getActor("GethActor")

  println(logo)

  lazy val logo = """
   _       __           __            
  | |     / /___  _____/ /_____  _____
  | | /| / / __ \/ ___/ //_/ _ \/ ___/
  | |/ |/ / /_/ / /  / ,< /  __/ /    
  |__/|__/\____/_/  /_/|_|\___/_/     """

}