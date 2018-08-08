//package io.upblockchain.worker.services
//
//import akka.actor.{ Actor, ActorSystem, OneForOneStrategy, SupervisorStrategy, Props }
//import akka.routing.{ DefaultResizer, RoundRobinPool }
//import akka.stream.ActorMaterializer
//import scala.concurrent.duration._
//import io.upblockchain.proto.jsonrpc._
//import akka.routing.FromConfig
//
//class ClientRouter()(implicit system: ActorSystem, mat: ActorMaterializer) extends Actor {
//
//  val routingDecider: PartialFunction[Throwable, SupervisorStrategy.Directive] = {
//    case _: Exception ⇒ SupervisorStrategy.Restart
//  }
//
//  val routerSupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 5 seconds)(
//    routingDecider.orElse(SupervisorStrategy.defaultDecider))
//
//  val resizer = DefaultResizer(
//    lowerBound = 2, upperBound = 50, pressureThreshold = 1, rampupRate = 1, backoffRate = 0.25, backoffThreshold = 0.25, messagesPerResize = 1)
//
//  private val router = {
//    system.actorOf(FromConfig.props(), "router3")
//    //        system.actorOf(
//    //          RoundRobinPool(nrOfInstances = 2, resizer = Some(resizer), supervisorStrategy = routerSupervisorStrategy)
//    //            .props(actor), "client-router")
//  }
//
//  def receive: Actor.Receive = {
//    case req: JsonRPCRequest ⇒
//      println("req ===>>>" + req)
//      router forward req
//    //    case reqs: JsonRPCRequestSeq ⇒
//    //      router forward reqs
//    case _ ⇒
//  }
//}
