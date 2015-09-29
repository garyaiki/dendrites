package org.gs.examples.account.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import org.gs.http._

class SavingsBalancesClientConfig()(implicit val system: ActorSystem, val mat: ActorMaterializer)
        extends BalancesProtocols {
  override implicit val materializer = mat
  val hostConfig = getHostConfig("akka-aggregator.savings-balances.http.interface",
          "akka-aggregator.savings-balances.http.port")
  val config = hostConfig._1
  val baseURL = configBaseUrl("akka-aggregator.savings-balances.http.path", hostConfig)
}
