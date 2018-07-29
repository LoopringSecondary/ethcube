package io.upblockchain.root

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import io.upblockchain.root.services.LookupActor
import scala.util.Random
import scala.concurrent.duration._
import akka.cluster.client.ClusterClient
import akka.cluster.client.ClusterClientSettings
import akka.actor.ActorPath
import io.upblockchain.proto.hello.HelloRequest
import com.google.inject.Guice
import io.upblockchain.root.modules.DefaultServiceModule
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Route
import io.upblockchain.root.routees.Router
import io.upblockchain.root.routees.RootRoute

object Main extends App {

  val injector = Guice.createInjector(DefaultServiceModule)

  implicit val system = injector.getInstance(classOf[ActorSystem])
  implicit val materializer = ActorMaterializer()

  //      implicit val system = injector.getInstance(classOf[ActorSystem])
  //    implicit val context = injector.getInstance(classOf[ExecutionContext])
  //    implicit val materializer = injector.getInstance(classOf[ActorMaterializer])

  //  val routees =
  //    pathPrefix("eth_") {
  //      get {
  //        path("aa") {
  //          complete("ok")
  //        }
  //      }
  //    }

  val r = injector.getInstance(classOf[RootRoute])

  Http().bindAndHandle(r(), "localhost", 8080)

  //  val config = ConfigFactory.load()
  //  implicit val system =
  //    ActorSystem("LookupSystem", config)

  //    val initialContacts = Set(
  //      ActorPath.fromString("akka.tcp://ClusterSystem@127.0.0.1:2552/system/receptionist"))

  // val c = system.actorOf(ClusterClient.props(ClusterClientSettings(system)), "client")

  // c ! ClusterClient.Send("/user/calculator", HelloRequest("Test"), localAffinity = true)
  //  c ! ClusterClient.SendToAll("/user/serviceB", "hi")

  //  val remotePath =
  //    "akka.tcp://CalculatorSystem@127.0.0.1:2552/user/calculator"
  //  val actor = system.actorOf(Props(classOf[LookupActor]), "lookupActor")
  //
  //  import system.dispatcher
  //
  //  system.scheduler.schedule(1 second, 1 second) {
  //    actor ! 100
  //  }

}