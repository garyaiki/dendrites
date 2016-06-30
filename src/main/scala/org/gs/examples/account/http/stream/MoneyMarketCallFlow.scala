package org.gs.examples.account.http.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import org.gs.examples.account.http.{BalancesProtocols, MoneyMarketBalancesClientConfig}
import org.gs.http.typedQueryResponse

/** Call Money Market Balances service. typedQueryResponse builds a GET request, calls the server,
  * mapPlain maps a failure, mapMoneyMarket maps good result. typedQueryResponse is a curried
  * function, only its initial argument list is passed here, the stream passes next two. Flow
  * mapAsync calls typedQueryResponse and passes 1 Future, the HTTP Response downstream
  *
  * @author Gary Struthers
  *
  */
class MoneyMarketCallFlow(implicit val system: ActorSystem, logger: LoggingAdapter, 
                val materializer: Materializer) extends BalancesProtocols {

  val clientConfig = new MoneyMarketBalancesClientConfig()
  val hostConfig = clientConfig.hostConfig
  val baseURL = clientConfig.baseURL
  val requestPath = clientConfig.requestPath

  def partial = typedQueryResponse(baseURL, requestPath, mapPlain, mapMoneyMarket) _ // curried
  
  def flow: Flow[Product, Either[String, AnyRef], NotUsed] = Flow[Product].mapAsync(1)(partial)
}
