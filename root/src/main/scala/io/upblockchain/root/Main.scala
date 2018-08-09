package io.upblockchain.root

import com.google.inject.Guice
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.Cluster
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import io.upblockchain.root.modules._
import io.upblockchain.root.routees.{ RootRoute }
import com.typesafe.config.Config
import io.upblockchain.root.router.{ EthWorkerGroup, RootRouter }

object Main extends App {

  val injector = Guice.createInjector(ServiceModule)

  implicit val system = injector.getInstance(classOf[ActorSystem])
  implicit val mat = injector.getInstance(classOf[ActorMaterializer])

  implicit val cluster = Cluster(system)
  //  val r = injector.getInstance(classOf[RootRoute])
  //  val config = injector.getInstance(classOf[Config])
  //
  //  Http().bindAndHandle(r(), config.getString("http.interface"), config.getInt("http.port"))
  println(logo)

  lazy val logo = """
      ____              __ 
     / __ \____  ____  / /_
    / /_/ / __ \/ __ \/ __/
   / _, _/ /_/ / /_/ / /_  
  /_/ |_|\____/\____/\__/  """

  val paths = Seq()
  system.actorOf(Props(new RootRouter(collection.immutable.Seq(paths: _*))))
}