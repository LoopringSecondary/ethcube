//package io.upblockchain.root.cluster
//
//import akka.actor._
//import akka.cluster.Cluster
//import akka.cluster.ClusterEvent._
//import io.upblockchain.root.routing.SimpleRoutingLogic
//import javax.inject.Inject
//import akka.routing.Router
//import akka.routing.ActorSelectionRoutee
//
//class SimpleClusterListener @Inject() (cluster: Cluster, logic: SimpleRoutingLogic) extends Actor with ActorLogging {
//
//  val router = Router(logic)
//
//  override def preStart(): Unit = {
//    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
//      classOf[MemberEvent], classOf[UnreachableMember])
//  }
//
//  override def postStop(): Unit = cluster.unsubscribe(self)
//
//  def receive = {
//    case MemberUp(member) ⇒
//      log.info("Member is Up: {}", member.address)
//      member.roles.contains("worker") match {
//        case true ⇒
//          val path = member.address.toString + "/user/worker"
//          val routee = ActorSelectionRoutee(context.actorSelection(path))
//          router.addRoutee(routee)
//        case _ ⇒
//
//      }
//
//    case UnreachableMember(member) ⇒
//      log.info("Member detected as unreachable: {}", member)
//    case MemberRemoved(member, previousStatus) ⇒
//      log.info(
//        "Member is Removed: {} after {}",
//        member.address, previousStatus)
//    case _: MemberEvent ⇒
//
//    // ignore
//  }
//
//  def removeMember: Unit = {
//    // router.removeRoutee(ref)
//  }
//}