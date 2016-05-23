package org.gs.examples.account.http

import akka.util.Timeout
import scala.concurrent.duration.MILLISECONDS
import org.gs.http._

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
  val timeout = new Timeout(config.getInt("dendrites.checking-balances.http.millis"),
      MILLISECONDS)
}
