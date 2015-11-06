package org.gs.examples.account.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.util.Timeout
import com.typesafe.config.{ Config, ConfigFactory }
import org.gs.http._
import scala.concurrent.duration.MILLISECONDS

class CheckingBalancesClientConfig() {

  val hostConfig = getHostConfig("dendrites.checking-balances.http.interface",
    "dendrites.checking-balances.http.port")
  val config = hostConfig._1
  val baseURL = configBaseUrl("dendrites.checking-balances.http.path", hostConfig)
  val timeout = new Timeout(config.getInt("dendrites.checking-balances.http.millis"),
      MILLISECONDS)
}
