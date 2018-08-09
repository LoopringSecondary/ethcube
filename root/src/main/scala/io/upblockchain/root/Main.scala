package io.upblockchain.root

import com.google.inject.Guice
import com.typesafe.config.Config

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import io.upblockchain.common.modules.SysAndConfigModule
import io.upblockchain.root.modules.ServiceModule
import io.upblockchain.root.rpc.RootEndpoints

object Main extends App {

  val injector = Guice.createInjector(new SysAndConfigModule(args), ServiceModule)
  val config = injector.getInstance(classOf[Config])

  implicit val system = injector.getInstance(classOf[ActorSystem])
  implicit val mat = injector.getInstance(classOf[ActorMaterializer])

  val r = injector.getInstance(classOf[RootEndpoints])

  // startup web app
  Http().bindAndHandle(r(), config.getString("http.host"), config.getInt("http.port"))

  println(logo)

  lazy val logo = """
      ____              __ 
     / __ \____  ____  / /_
    / /_/ / __ \/ __ \/ __/
   / _, _/ /_/ / /_/ / /_  
  /_/ |_|\____/\____/\__/  """

}