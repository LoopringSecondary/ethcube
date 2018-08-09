/*
 * Copyright 2018 Loopring Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.loopring.ethcube.root

import com.google.inject.Guice

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import org.loopring.ethcube.root.modules._
import org.loopring.ethcube.root.routees.RootRoute
import com.typesafe.config.Config

object Main extends App {

  val injector = Guice.createInjector(ServiceModule)

  implicit val system = injector.getInstance(classOf[ActorSystem])
  implicit val mat = injector.getInstance(classOf[ActorMaterializer])

  val r = injector.getInstance(classOf[RootRoute])
  val config = injector.getInstance(classOf[Config])

  Http().bindAndHandle(r(), config.getString("http.interface"), config.getInt("http.port"))

  println(logo)

  lazy val logo = """
      ____              __ 
     / __ \____  ____  / /_
    / /_/ / __ \/ __ \/ __/
   / _, _/ /_/ / /_/ / /_  
  /_/ |_|\____/\____/\__/  """

}