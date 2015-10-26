package org.gs.examples.account.http.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import org.gs.examples.account.http._
import org.gs.http._
import scala.concurrent.Future

class MoneyMarketCallFlow(implicit val system: ActorSystem, logger: LoggingAdapter, 
                val materializer: Materializer) extends BalancesProtocols {

  val clientConfig = new MoneyMarketBalancesClientConfig()
  val hostConfig = clientConfig.hostConfig
  val baseURL = clientConfig.baseURL

  def partial = typedQueryResponse(baseURL, mapPlain, mapMoneyMarket) _
  
  def flow: Flow[Product, Future[Either[String, AnyRef]], Unit] = Flow[Product].map(partial)

}