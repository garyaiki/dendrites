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
package org.gs.examples.account.http.stream

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.{FlowShape, Materializer, UniformFanOutShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, ZipWith}
import scala.concurrent.ExecutionContext
import org.gs.stream.leftRightFlow
import org.gs.stream.logLeftRightFlow

/** Create Graph that calls Checking, MoneyMarket, Savings services in parallel, waits for them all
  * then groups failures and successes
  *
  * {{{
  * bcast ~> check ~> zip.in0
  * bcast ~> mm ~> zip.in1
  * bcast ~> savings ~> zip.in2
  * }}}
  * @author Gary Struthers
  */
class ParallelCallFlow(implicit val ec: ExecutionContext,
                       system: ActorSystem,
                       logger: LoggingAdapter,
                       val materializer: Materializer) {

  def zipper = ZipWith((in0: Either[String, AnyRef],
                        in1: Either[String, AnyRef],
                        in2: Either[String, AnyRef]) => (in0, in1, in2))

  val ccf = new CheckingCallFlow
  val mmcf = new MoneyMarketCallFlow
  val scf = new SavingsCallFlow

  import GraphDSL.Implicits._
  // Create Graph in Shape of a Flow
  val flowGraph = GraphDSL.create() { implicit builder =>
    val bcast: UniformFanOutShape[Product, Product] = builder.add(Broadcast[Product](3))
    val check: FlowShape[Product,Either[String, AnyRef]] = builder.add(ccf.flow)
    val mm: FlowShape[Product,Either[String, AnyRef]] = builder.add(mmcf.flow)
    val savings: FlowShape[Product,Either[String, AnyRef]] = builder.add(scf.flow)
    val zip = builder.add(zipper)

    bcast ~> check ~> zip.in0
    bcast ~> mm ~> zip.in1
    bcast ~> savings ~> zip.in2
    FlowShape(bcast.in, zip.out)
  }.named("calls")
  // Cast Graph to Flow
  val asFlow = Flow.fromGraph(flowGraph)
  // Map tuple3 from flowGraph
  val fgLR = GraphDSL.create() { implicit builder =>
    val fgCalls = builder.add(asFlow)
    val fgLR = builder.add(leftRightFlow) // results combiner

    fgCalls ~> fgLR
    FlowShape(fgCalls.in, fgLR.outlet)
  }.named("callsLeftRight")
  val wrappedCallsLRFlow = Flow.fromGraph(fgLR)

  // Map tuple3 from flowGraph
  val fgLogLeftPassRight = GraphDSL.create() { implicit builder =>
    val fgCalls = builder.add(asFlow)
    val fgLR = builder.add(logLeftRightFlow) // results combiner

    fgCalls ~> fgLR
    FlowShape(fgCalls.in, fgLR.outlet)
  }.named("callsLogLeftRight")
  val wrappedCallsLogLeftPassRightFlow = Flow.fromGraph(fgLogLeftPassRight)
}
