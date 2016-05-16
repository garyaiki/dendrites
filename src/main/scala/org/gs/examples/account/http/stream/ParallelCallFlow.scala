package org.gs.examples.account.http.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.{Materializer, FlowShape, UniformFanOutShape}
import akka.stream.scaladsl.{ Broadcast, Flow, GraphDSL, ZipWith}
import org.gs.stream.leftRightFlow

/** Create Graph that calls Checking, MoneyMarket, Savings services in parallel, waits for them all
  *
  * @author Gary Struthers
  * @param system implicit ActorSystem
  * @param logger implicit logger
  * @param materializer implicit Materializer
  */
class ParallelCallFlow(implicit val system: ActorSystem, logger: LoggingAdapter, 
                val materializer: Materializer) {
  
  def zipper = ZipWith((in0: Either[String, AnyRef],
                        in1: Either[String, AnyRef],
                        in2: Either[String, AnyRef]) => (in0, in1, in2))

  val ccf = new CheckingCallFlow
  val mmcf = new MoneyMarketCallFlow
  val scf = new SavingsCallFlow

  import GraphDSL.Implicits._ 
  // Create Graph in Shape of a Flow
  val fg = GraphDSL.create() { implicit builder =>
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
  val wrappedFlow = Flow.fromGraph(fg)
  // Map tuple3 from fg
  val fgLR = GraphDSL.create() { implicit builder =>
    val fgCalls = builder.add(wrappedFlow)
    val fgLR = builder.add(leftRightFlow)
    
    fgCalls ~> fgLR
    FlowShape(fgCalls.in, fgLR.outlet)
  }.named("callsLeftRight")
  val wrappedCallsLRFlow = Flow.fromGraph(fgLR)
}
