/**
  */
package org.gs.examples.account.http

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

/** @author garystruthers
  *
  */
object BalancesServer extends App with BalancesService {
  override implicit val system = ActorSystem("akka-aggregator")
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("balancesIP"), config.getInt("balancesPort"))
}