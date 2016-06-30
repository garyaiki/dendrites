package org.gs.examples.account.http

import akka.util.Timeout
import scala.concurrent.duration.MILLISECONDS
import org.gs.http.{configBaseUrl, configRequestPath, getHostConfig}

/** Read config for Savings Balances Client
  *
  * @author Gary Struthers
  *
  */
class SavingsBalancesClientConfig() {

  val hostConfig = getHostConfig("dendrites.savings-balances.http.interface",
    "dendrites.savings-balances.http.port")
  val config = hostConfig._1
  val baseURL = configBaseUrl("dendrites.savings-balances.http.path", hostConfig)
  val requestPath = configRequestPath("dendrites.savings-balances.http.requestPath", config)
  val timeout = new Timeout(config.getInt("dendrites.savings-balances.http.millis"),
      MILLISECONDS)
}
