package io.upblockchain.root

import com.google.inject.Guice

import akka.actor.ActorSystem
import io.upblockchain.root.modules.ServiceModule
import io.upblockchain.root.routees.RootRoute
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object Main extends App {

  val injector = Guice.createInjector(ServiceModule)

  implicit val system = injector.getInstance(classOf[ActorSystem])
  implicit val mat = injector.getInstance(classOf[ActorMaterializer])
  

  val r = injector.getInstance(classOf[RootRoute])
  Http().bindAndHandle(r(), "localhost", 8080)

  lazy val logo = """
      ____              __ 
     / __ \____  ____  / /_
    / /_/ / __ \/ __ \/ __/
   / _, _/ /_/ / /_/ / /_  
  /_/ |_|\____/\____/\__/  """

}