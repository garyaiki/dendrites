/** Copyright 2016 Gary Struthers

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.github.garyaiki.dendrites.examples.account.http

import akka.util.Timeout
import scala.concurrent.duration.MILLISECONDS
import com.github.garyaiki.dendrites.http.{configBaseUrl, configRequestPath, getHostConfig}

/** Read config for Money Market Balances Client
  *
  * @author Gary Struthers
  *
  */
class MoneyMarketBalancesClientConfig() {

  val hostConfig = getHostConfig("dendrites.money-market-balances.http.interface",
    "dendrites.money-market-balances.http.port")
  val config = hostConfig._1
  val baseURL = configBaseUrl("dendrites.money-market-balances.http.path", hostConfig)
  val requestPath = configRequestPath("dendrites.money-market-balances.http.requestPath", config)
  val timeout = new Timeout(config.getInt("dendrites.money-market-balances.http.millis"),
      MILLISECONDS)
}
