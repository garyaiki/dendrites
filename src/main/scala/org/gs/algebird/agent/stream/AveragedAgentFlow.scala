package org.gs.algebird.agent.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.twitter.algebird.AveragedValue
import scala.concurrent.Future
import org.gs.algebird.agent.AveragedAgent

/** Flow to update AveragedValue Agent
  *
  * @author Gary Struthers
  * @param avgAgent AveragedAgent
  */
class AveragedAgentFlow(avgAgent: AveragedAgent)
  extends GraphStage[FlowShape[AveragedValue, Future[AveragedValue]]] {

  val in = Inlet[AveragedValue]("AveragedValue in")
  val out = Outlet[Future[AveragedValue]]("Future AveragedValue out")
  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val elem = grab(in)
          push(out, avgAgent.alter(elem))
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
