package org.gs.algebird.agent.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.twitter.algebird.{HLL, HyperLogLogAggregator, HyperLogLogMonoid}
import scala.concurrent.Future
import org.gs.algebird.agent.HyperLogLogAgent

/** Flow to update HyperLogLogAgent Agent
  *
  * @author Gary Struthers
  * @param hllAgent HyperLogLogAgent
  */
class HyperLogLogAgentFlow(hllAgent: HyperLogLogAgent)
  extends GraphStage[FlowShape[HLL, Future[HLL]]] {

  val in = Inlet[HLL]("HLL in")
  val out = Outlet[Future[HLL]]("Future HLL out")
  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val elem = grab(in)
          push(out, hllAgent.alter(elem))
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
