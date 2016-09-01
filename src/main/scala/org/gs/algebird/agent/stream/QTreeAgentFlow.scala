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
package org.gs.algebird.agent.stream

import akka.NotUsed
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.scaladsl.{Flow, Sink}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.twitter.algebird.{QTree, QTreeSemigroup}
import scala.concurrent.Future
import scala.reflect.runtime.universe.TypeTag
import org.gs.algebird.agent.QTreeAgent
import org.gs.algebird.typeclasses.QTreeLike

/** Flow to update QTreeAgent
  *
  * @tparam A: QTreeLike either BigDecimal, BigInt, Double, Float, Int or Long
  * @param qtAgent QTreeAgent
  * @author Gary Struthers
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

object QTreeAgentFlow {

  /** Compose QTreeAgentFlow & Sink
    *
  	*	@tparam A is a QTreeLike with a TypeTag
  	* @param qtAgent Akka Agent builds QTree
  	* @return Sink that accepts Seq[A]
  	*/  
  def compositeSink[A: TypeTag: QTreeLike](qtAgent: QTreeAgent[A]): Sink[Seq[A], NotUsed] = {
    val qtAgtFlow = new QTreeAgentFlow(qtAgent)
    val ffg = Flow.fromGraph(qtAgtFlow)
    ffg.to(Sink.ignore).named("SeqToAvgAgentSink")
  }  
}
