/**
  */
package org.gs.examples.account.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

/** @author garystruthers
  *
  */
object BalancesServer extends App with BalancesService {
  override implicit val system = ActorSystem("dendrites")
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("balancesIP"), config.getInt("balancesPort"))
}