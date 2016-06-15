package org.gs.algebird.agent.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.twitter.algebird.{QTree, QTreeSemigroup}
import scala.concurrent.Future
import scala.reflect.runtime.universe.TypeTag
import org.gs.algebird.agent.QTreeAgent
import org.gs.algebird.typeclasses.QTreeLike

/** Flow to update QTreeAgent
  *
  * @author Gary Struthers
  * @tparam A: QTreeLike either BigDecimal, BigInt, Double, Float, Int or Long
  * @param qtAgent QTreeAgent
  */
class QTreeAgentFlow[A: QTreeLike](qtAgent: QTreeAgent[A])
  extends GraphStage[FlowShape[Seq[A], Future[QTree[A]]]] {

  val in = Inlet[Seq[A]]("QTree like values in")
  val out = Outlet[Future[QTree[A]]]("Future QTree out")
  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val elem = grab(in)
          push(out, qtAgent.alter(elem))
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
