package org.gs.algebird.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.twitter.algebird.{HLL, HyperLogLogAggregator}
import org.gs.algebird.createHLL
import org.gs.algebird.AlgebirdConfigurer.hyperLogLogBits
import org.gs.algebird.typeclasses.HyperLogLogLike

class CreateHLLFlow[A: HyperLogLogLike](bits: Int = hyperLogLogBits)
  extends GraphStage[FlowShape[Seq[A], HLL]] {

  val in = Inlet[Seq[A]]("CreateHLLFlow in")
  val out = Outlet[HLL]("CreateHLLFlow out")
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
}
