package org.gs.examples.account.http

import akka.util.Timeout
import org.gs.http._
import scala.concurrent.duration.MILLISECONDS

class SavingsBalancesClientConfig() {

  val hostConfig = getHostConfig("dendrites.savings-balances.http.interface",
    "dendrites.savings-balances.http.port")
  val config = hostConfig._1
  val baseURL = configBaseUrl("dendrites.savings-balances.http.path", hostConfig)
  val timeout = new Timeout(config.getInt("dendrites.savings-balances.http.millis"),
      MILLISECONDS)
}
