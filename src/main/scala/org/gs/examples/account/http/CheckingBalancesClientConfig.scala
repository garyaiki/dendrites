package org.gs.examples.account.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.util.Timeout
import com.typesafe.config.{ Config, ConfigFactory }
import org.gs.http._
import scala.concurrent.duration.MILLISECONDS

class CheckingBalancesClientConfig() {

  val hostConfig = getHostConfig("akka-aggregator.checking-balances.http.interface",
    "akka-aggregator.checking-balances.http.port")
  val config = hostConfig._1
  val baseURL = configBaseUrl("akka-aggregator.checking-balances.http.path", hostConfig)
  val timeout = new Timeout(config.getInt("akka-aggregator.checking-balances.http.millis"),
      MILLISECONDS)
}
