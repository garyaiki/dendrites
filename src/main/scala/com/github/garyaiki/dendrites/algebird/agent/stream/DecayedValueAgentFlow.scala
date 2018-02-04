/**
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
package com.github.garyaiki.dendrites.algebird.agent.stream

import akka.NotUsed
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.scaladsl.{Flow, Sink}
import com.twitter.algebird.DecayedValue
import java.time.Instant
import scala.concurrent.Future
import scala.reflect.runtime.universe.TypeTag
import com.github.garyaiki.dendrites.algebird.agent.DecayedValueAgent
import com.github.garyaiki.dendrites.algebird.stream.ZipTimeFlow

/** Flow to update DecayedValue Agent
  *
  * @deprecated
  * @param dvAgent DecayedValueAgent
  * @author Gary Struthers
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
          push(out, dvAgent.alter(elem))
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = pull(in)
      })
    }
  }
}

object DecayedValueAgentFlow {

  /** Generate a time value as a Double
    *
    * tparam A: Numeric : TypeTag
    * @param ignore is same numeric type passed to flow, this function ignores it
    * @return Now as a Double
    */
  def nowMillis[A: Numeric : TypeTag](ignore: A = null): Double = Instant.now.toEpochMilli.toDouble

  /** Compose ZipTimeFlow & dvFlow & DecayedValueAgentFlow
    *
    * @tparam A Numeric that has implicit conversions to Double with a TypeTag
    * @param dvAgent Akka Agent accumulates DecayedValues
    * @param time function that adds time value, needed by DecayedValue
    * @return Future for Agents updated value
    */
  def compositeFlow[A: TypeTag : Numeric](dvAgent: DecayedValueAgent, time:A => Double):
        Flow[Seq[A], Future[Seq[DecayedValue]], NotUsed] = {

    val zt = new ZipTimeFlow[A](time)
    val ffg = Flow.fromGraph(zt)
    val dvAgtFlow = new DecayedValueAgentFlow(dvAgent)
    ffg.via(dvAgtFlow).named("SeqToDVAgent")
  }

  /** Compose ZipTimeFlow & dvFlow & DecayedValueAgentFlow & Sink
    *
    * @tparam A Numeric that has implicit conversions to Double with a TypeTag
    * @param dvAgent Akka Agent accumulates DecayedValues
    * @param time function that adds time value, needed by DecayedValue
    * @return Sink that accepts Seq[A]
    */
  def compositeSink[A: TypeTag: Numeric](dvAgent: DecayedValueAgent, time:A => Double): Sink[Seq[A], NotUsed] = {
    compositeFlow(dvAgent, time).to(Sink.ignore)
  }
}
