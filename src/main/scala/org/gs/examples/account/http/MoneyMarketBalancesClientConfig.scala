package org.gs.examples.account.http


import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import org.gs.http._

class MoneyMarketBalancesClientConfig()
        (implicit val system: ActorSystem, val mat: ActorMaterializer) extends BalancesProtocols {
  override implicit val materializer = mat
  val hostConfig = getHostConfig("akka-aggregator.money-market-balances.http.interface",
          "akka-aggregator.money-market-balances.http.port")
  val config = hostConfig._1
  val baseURL = configBaseUrl("akka-aggregator.money-market-balances.http.path", hostConfig)
}
