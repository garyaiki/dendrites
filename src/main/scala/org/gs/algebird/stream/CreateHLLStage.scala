package org.gs.algebird.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.twitter.algebird.{HLL, HyperLogLogAggregator}

import org.gs.algebird.createHLL
import org.gs.algebird.AlgebirdConfigurer.hyperLogLogBits
import org.gs.algebird.typeclasses.HyperLogLogLike

class CreateHLLStage[A: HyperLogLogLike](bits: Int = hyperLogLogBits)
  extends GraphStage[FlowShape[Seq[A], HLL]] {

  val in = Inlet[Seq[A]]("CreateHLLStage in")
  val out = Outlet[HLL]("CreateHLLStage out")
  override val shape = FlowShape.of(in, out)
  implicit val ag = HyperLogLogAggregator(bits)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val elem = grab(in)
          push(out, createHLL(elem))
        }
      })
  
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
  }
//  override def onPush(elem: Seq[A], ctx: Context[HLL]): SyncDirective = ctx.push(createHLL(elem))
}
