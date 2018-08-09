package io.upblockchain

import akka.actor.ActorRef
import com.google.inject._
import com.google.inject.name.Names

package object common {

  implicit class ActorInjector(injector: Injector) {
    def getActor(name: String): ActorRef = {
      injector.getInstance(Key.get(classOf[ActorRef], Names.named(name)))
    }
  }
}