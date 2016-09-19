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
import akka.event.LoggingAdapter
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.scaladsl.{Flow, Sink}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.twitter.algebird.AveragedValue
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.runtime.universe.TypeTag
import org.gs.algebird.agent.AveragedAgent
import org.gs.algebird.stream.avgFlow

/** Flow to update AveragedValue Agent
  *
  * @param avgAgent AveragedAgent
  * @author Gary Struthers
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

object AveragedAgentFlow {

  /** Compose avgFlow & AveragedAgentFlow
    *
    *  @tparam A is a Numeric with a TypeTag
    * @param avgAgent Akka Agent accumulates AveragedValue
    * @return Future for Agents updated value
    */
  def compositeFlow[A: TypeTag: Numeric](avgAgent: AveragedAgent):
          Flow[Seq[A], Future[AveragedValue], NotUsed] = {
    val agnt = new AveragedAgentFlow(avgAgent)
    avgFlow.via(agnt).named("SeqToAvgAgent")
  }

  /** Compose avgFlow & AveragedAgentFlow & Sink
    *
    * @tparam A is a Numeric with a TypeTag
    * @param avgAgent Akka Agent accumulates AveragedValue
    * @return Sink that accepts Seq[A]
    */
  def compositeSink[A: TypeTag: Numeric](avgAgent: AveragedAgent)
          (implicit log: LoggingAdapter, ec: ExecutionContext): Sink[Seq[A], NotUsed] = {

    compositeFlow(avgAgent).to(Sink.ignore).named("SeqToAvgAgentSink")
  }
}
