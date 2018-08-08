package io.upblockchain.root

import com.google.inject.Guice

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import io.upblockchain.root.modules._
import com.typesafe.config.Config
import io.upblockchain.common.modules.ConfigModule
import io.upblockchain.root.rpc.RootEndpoints

object Main extends App {

  val injector = Guice.createInjector(new ConfigModule(args), ServiceModule)
  val config = injector.getInstance(classOf[Config])

  implicit val system = injector.getInstance(classOf[ActorSystem])
  implicit val mat = injector.getInstance(classOf[ActorMaterializer])

  val r = injector.getInstance(classOf[RootEndpoints])

  val server = Http().bindAndHandle(r(), config.getString("http.interface"), config.getInt("http.port"))

  //  server.onComplete { x =>
  //
  //  }

  println(logo)

  lazy val logo = """
      ____              __ 
     / __ \____  ____  / /_
    / /_/ / __ \/ __ \/ __/
   / _, _/ /_/ / /_/ / /_  
  /_/ |_|\____/\____/\__/  """

}