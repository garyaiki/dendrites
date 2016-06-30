package org.gs.examples.account.http

import akka.util.Timeout
import scala.concurrent.duration.MILLISECONDS
import org.gs.http.{configBaseUrl, configRequestPath, getHostConfig}

/** Read config for Checking Balances Client
  *
  * @author Gary Struthers
  *
  */
class CheckingBalancesClientConfig() {

  val hostConfig = getHostConfig("dendrites.checking-balances.http.interface",
    "dendrites.checking-balances.http.port")
  val config = hostConfig._1
  val baseURL = configBaseUrl("dendrites.checking-balances.http.path", hostConfig)
  val requestPath = configRequestPath("dendrites.checking-balances.http.requestPath", config)
  val timeout = new Timeout(config.getInt("dendrites.checking-balances.http.millis"),
      MILLISECONDS)
}
