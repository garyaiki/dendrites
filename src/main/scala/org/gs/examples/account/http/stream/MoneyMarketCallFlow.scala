package org.gs.examples.account.http.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import org.gs.examples.account.http._
import org.gs.http._

class MoneyMarketCallFlow(implicit val system: ActorSystem, logger: LoggingAdapter, 
                val materializer: Materializer) extends BalancesProtocols {

  val clientConfig = new MoneyMarketBalancesClientConfig()
  val hostConfig = clientConfig.hostConfig
  val baseURL = clientConfig.baseURL

  def partial = typedQueryResponse(baseURL, mapPlain, mapMoneyMarket) _
  
  def flow: Flow[Product, Either[String, AnyRef], NotUsed] = Flow[Product].mapAsync(1)(partial)
}
