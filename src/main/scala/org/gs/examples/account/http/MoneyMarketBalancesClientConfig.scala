package org.gs.examples.account.http

import akka.util.Timeout
import org.gs.http._
import scala.concurrent.duration.MILLISECONDS

class MoneyMarketBalancesClientConfig() {

  val hostConfig = getHostConfig("akka-aggregator.money-market-balances.http.interface",
    "akka-aggregator.money-market-balances.http.port")
  val config = hostConfig._1
  val baseURL = configBaseUrl("akka-aggregator.money-market-balances.http.path", hostConfig)
  val timeout = new Timeout(config.getInt("akka-aggregator.money-market-balances.http.millis"),
      MILLISECONDS)
}
