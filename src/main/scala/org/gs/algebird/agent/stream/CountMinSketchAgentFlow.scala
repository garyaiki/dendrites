package org.gs.algebird.agent.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.twitter.algebird.{CMS, CMSHasher}
import scala.concurrent.Future
import org.gs.algebird.agent.CountMinSketchAgent

/** Flow to update CountMinSketch Agent
  *
  * @author Gary Struthers
  * @param cmsAgent CountMinSketchAgent
  */
class CountMinSketchAgentFlow[K: Ordering: CMSHasher](cmsAgent: CountMinSketchAgent[K])
  extends GraphStage[FlowShape[CMS[K], Future[CMS[K]]]] {

  val in = Inlet[CMS[K]]("CMS in")
  val out = Outlet[Future[CMS[K]]]("Future CMS out")
  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val elem = grab(in)
          push(out, cmsAgent.alter(elem))
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
