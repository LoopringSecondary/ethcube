package io.upblockchain.root.modules

import com.google.inject.AbstractModule
import akka.actor.ActorSystem
import com.google.inject.{ Provides, Singleton }
import akka.actor.ActorRef
import javax.inject.Inject
import io.upblockchain.root.services.LookupActor
import akka.actor.Props
import akka.http.scaladsl.server.Route
import io.upblockchain.root.routees.Router
import net.codingwell.scalaguice.ScalaModule
import io.upblockchain.root.routees.RootRoute
import io.upblockchain.root.routees.EthJsonRPCRoute

trait DefaultServiceModule extends AbstractModule with ScalaModule {

  override def configure: Unit = {
    bindRoute
  }

  @Provides @Singleton
  def provideActorSystem: ActorSystem = {
    ActorSystem("ClusterSystem")
  }
  
  private[this] def bindRoute {
    bind[EthJsonRPCRoute]
    bind[RootRoute]
  }

  //  @Provides @Singleton
  //  def provideActor(@Inject() system: ActorSystem): ActorRef = {
  //    system.actorOf(Props((classOf[LookupActor])), "")
  //  }

  //  @Provides @Singleton
  //  def provideRoutees: Seq[Router] = {
  //    // new EthJsonRPCRoutes().routees
  //
  //
  //
  //  }

}

object DefaultServiceModule extends DefaultServiceModule