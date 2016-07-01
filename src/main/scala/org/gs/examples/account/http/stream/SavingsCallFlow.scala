package org.gs.examples.account.http.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import org.gs.examples.account.http.{BalancesProtocols, SavingsBalancesClientConfig}
import org.gs.http.caseClassToGetQuery
import org.gs.http.stream.{TypedQueryFlow, TypedQueryResponseFlow, TypedResponseFlow}

/** Call Savings Balances service. Build a GET request, call the server,
  * mapPlain maps a failure, mapSavings maps good result.
  *
  * @author Gary Struthers
  *
  */
class SavingsCallFlow(implicit val system: ActorSystem, logger: LoggingAdapter, 
                val materializer: Materializer) extends BalancesProtocols {

  val clientConfig = new SavingsBalancesClientConfig()
  val hostConfig = clientConfig.hostConfig
  val baseURL = clientConfig.baseURL
  val requestPath = clientConfig.requestPath
  val queryFlow = new TypedQueryFlow(baseURL, requestPath, caseClassToGetQuery)
  val responseFlow = new TypedResponseFlow(mapPlain, mapSavings)
  val tqr = new TypedQueryResponseFlow(queryFlow, responseFlow)

  def flow: Flow[Product, Either[String, AnyRef], NotUsed] = tqr.flow
}
