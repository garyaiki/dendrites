package org.gs.algebird.agent.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.twitter.algebird.DecayedValue
import scala.concurrent.Future
import org.gs.algebird.agent.DecayedValueAgent

/** Flow to update DecayedValue Agent
  *
  * @author Gary Struthers
  * @param dvAgent DecayedValueAgent
  */
class DecayedValueAgentFlow(dvAgent: DecayedValueAgent)
  extends GraphStage[FlowShape[Seq[(Double, Double)], Future[Seq[DecayedValue]]]] {

  val in = Inlet[Seq[(Double, Double)]]("Vals with times in")
  val out = Outlet[Future[Seq[DecayedValue]]]("Future DecayedValues out")
  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val elem = grab(in)
          System.out.print(s"elem:$elem")
          push(out, dvAgent.alter(elem))
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
