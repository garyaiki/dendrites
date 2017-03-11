/**

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
package com.github.garyaiki.dendrites.examples.account.http.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.{ActorAttributes, Materializer}
import akka.stream.scaladsl.Flow
import scala.concurrent.ExecutionContext
import com.github.garyaiki.dendrites.examples.account.http.{BalancesProtocols, CheckingBalancesClientConfig}
import com.github.garyaiki.dendrites.http.caseClassToGetQuery
import com.github.garyaiki.dendrites.http.stream.{TypedQueryFlow, TypedQueryResponseFlow, TypedResponseFlow}
import com.github.garyaiki.dendrites.http.stream.TypedResponseFlow.decider

/** Call Checking Balances service. Build a GET request, call the server,
  * mapPlain maps a failure message, mapChecking maps good result.
  *
  * @author Gary Struthers
  *
  */
class CheckingCallFlow(implicit ec: ExecutionContext,
  val system: ActorSystem,
  log: LoggingAdapter,
  val mat: Materializer)  extends BalancesProtocols {

  val clientConfig = new CheckingBalancesClientConfig()
  val baseURL = clientConfig.baseURL
  val requestPath = clientConfig.requestPath
  val queryFlow = new TypedQueryFlow(baseURL, requestPath, caseClassToGetQuery)
  val responseFlow = new TypedResponseFlow(mapPlain, mapChecking)

  val tqr = new TypedQueryResponseFlow(queryFlow, responseFlow)

  def flow: Flow[Product, Either[String, AnyRef], NotUsed] = {
    val flow = tqr.flow
    flow.withAttributes(ActorAttributes.supervisionStrategy(decider))
  }
}
