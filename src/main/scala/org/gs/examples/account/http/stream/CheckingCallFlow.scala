package org.gs.examples.account.http.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import org.gs.examples.account.http.{BalancesProtocols, CheckingBalancesClientConfig}
import org.gs.http.typedQueryResponse

/** Call Checking Balances service. typedQueryResponse builds a GET request, calls the server,
  * mapPlain maps a failure message, mapChecking maps good result. typedQueryResponse is a curried
  * function, only its initial argument list is passed here, the stream passes the rest. Flow
  * mapAsync calls typedQueryResponse and passes 1 Future, the HTTP Response downstream
  *
  * @author Gary Struthers
  *
  */
class CheckingCallFlow(implicit val system: ActorSystem, logger: LoggingAdapter, 
  val materializer: Materializer) extends BalancesProtocols {

  val clientConfig = new CheckingBalancesClientConfig()
  val hostConfig = clientConfig.hostConfig
  val baseURL = clientConfig.baseURL

  def partial = typedQueryResponse(baseURL, mapPlain, mapChecking) _ // curried

  def flow: Flow[Product, Either[String, AnyRef], NotUsed] = Flow[Product].mapAsync(1)(partial)
}
