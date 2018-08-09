//package io.upblockchain.root.router
//
//import java.util.concurrent.TimeUnit
//
//import akka.actor.{ Actor, ActorRef, ActorSystem, UntypedActor }
//import akka.cluster.Cluster
//import akka.cluster.ClusterEvent._
//import akka.routing._
//import akka.stream.ActorMaterializer
////import io.upblockchain.root.Main.{ paths, system }
//import akka.pattern.ask
//import akka.util.Timeout
//
//import scala.collection.immutable
//import scala.concurrent.Future
//
///*
//
//  Copyright 2017 Loopring Project Ltd (Loopring Foundation).
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
//*/
//
//class RootRouter(paths: immutable.Iterable[String])(implicit cluster: Cluster, mat: ActorMaterializer) extends Actor {
//  import context.dispatcher
//
//  var router = Router(new EthRoutingLogic())
//  //  val router: ActorRef =
//  //    system.actorOf(
//  //      EthWorkerGroup(paths).props(),
//  //      name = "root-router")
//
//  override def preStart(): Unit = {
//    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
//      classOf[MemberEvent], classOf[UnreachableMember])
//  }
//
//  implicit val timeout: Timeout = Timeout.apply(3, TimeUnit.SECONDS)
//  def receive: Actor.Receive = {
//    case MemberUp(member) ⇒
//      if (member.roles.contains("worker")) {
//        var path = member.address.toString + "/user/worker"
//        println("MemberUpMemberUpMemberUp", path)
//        val routee = ActorSelectionRoutee(context.actorSelection(path))
//        router = router.addRoutee(routee)
//        println(router.routees)
//        router.route(1, null)
//
//      }
//    case UnreachableMember(member) ⇒
//      if (member.roles.contains("worker")) {
//        var path = member.address.toString + "/user/worker"
//        val removeRoutee = ActorSelectionRoutee(context.actorSelection(path))
//        router = router.removeRoutee(removeRoutee)
//      }
//    case MemberRemoved(member, previousStatus) ⇒
//      if (member.roles.contains("worker")) {
//        var path = member.address.toString + "/user/worker"
//        val removeRoutee = ActorSelectionRoutee(context.actorSelection(path))
//        router = router.removeRoutee(removeRoutee)
//      }
//    //    case req: String ⇒
//    //      router forward req
//    //    case req: Int ⇒
//    //      router forward req
//    case _ ⇒
//  }
//
//}
