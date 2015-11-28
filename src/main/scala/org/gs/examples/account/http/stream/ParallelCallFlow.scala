package org.gs.examples.account.http.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.{Materializer, FlowShape, UniformFanOutShape}
import akka.stream.scaladsl.{ Broadcast, Flow, FlowGraph, ZipWith}

class ParallelCallFlow(implicit val system: ActorSystem, logger: LoggingAdapter, 
                val materializer: Materializer) {
  
  def zipper = ZipWith((in0: Either[String, AnyRef],
                        in1: Either[String, AnyRef],
                        in2: Either[String, AnyRef]) => (in0, in1, in2))

  val ccf = new CheckingCallFlow
  val ccfFlow = ccf.flow
  val mmcf = new MoneyMarketCallFlow
  val mmfFlow = mmcf.flow
  val scf = new SavingsCallFlow
  val scfFlow = scf.flow

  import FlowGraph.Implicits._ 
  val fg = FlowGraph.create() { implicit builder =>
    val bcast: UniformFanOutShape[Product, Product] = builder.add(Broadcast[Product](3))
    
    val check: FlowShape[Product,Either[String, AnyRef]] = builder.add(ccfFlow)
    val mm: FlowShape[Product,Either[String, AnyRef]] = builder.add(mmfFlow)
    val savings: FlowShape[Product,Either[String, AnyRef]] = builder.add(scfFlow)
    val zip = builder.add(zipper)
    
    bcast ~> check ~> zip.in0
    bcast ~> mm ~> zip.in1
    bcast ~> savings ~> zip.in2
    FlowShape(bcast.in, zip.out)
  }.named("calls")
  val wrappedFlow = Flow.fromGraph(fg)
  
  val fgLR = FlowGraph.create() { implicit builder =>
    val fgCalls = builder.add(wrappedFlow)
    val fgLR = builder.add(leftRightFlow)
    
    fgCalls ~> fgLR
    FlowShape(fgCalls.inlet, fgLR.outlet)
  }.named("callsLeftRight")
  val wrappedCallsLRFlow = Flow.fromGraph(fgLR)

}
