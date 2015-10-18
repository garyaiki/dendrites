package org.gs.examples.account.http

import akka.util.Timeout
import org.gs.http._
import scala.concurrent.duration.MILLISECONDS

class SavingsBalancesClientConfig() {

  val hostConfig = getHostConfig("akka-aggregator.savings-balances.http.interface",
    "akka-aggregator.savings-balances.http.port")
  val config = hostConfig._1
  val baseURL = configBaseUrl("akka-aggregator.savings-balances.http.path", hostConfig)
  val timeout = new Timeout(config.getInt("akka-aggregator.savings-balances.http.millis"),
      MILLISECONDS)
}
