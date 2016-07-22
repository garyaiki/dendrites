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
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.scaladsl.{Flow, Sink}
import com.twitter.algebird.{HLL, HyperLogLogAggregator, HyperLogLogMonoid}
import scala.concurrent.Future
import scala.reflect.runtime.universe.TypeTag
import org.gs.algebird.createHLL
import org.gs.algebird.AlgebirdConfigurer.hyperLogLogBits
import org.gs.algebird.agent.HyperLogLogAgent
import org.gs.algebird.stream.CreateHLLFlow
import org.gs.algebird.typeclasses.HyperLogLogLike

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

object HyperLogLogAgentFlow {

  /** Compose hllFlow & HyperLogLogAgentFlow
    *
    * @see [[org.gs.algebird.typeclasses.HyperLogLogLike]]
  	*	@tparam A is a HyperLogLogLike with a TypeTag
  	* @param hllAgent Akka Agent accumulates HLL values
  	* @return Future for Agents updated value
  	*/
  def compositeFlow[A: TypeTag: HyperLogLogLike](hllAgent: HyperLogLogAgent):
          Flow[Seq[A], Future[HLL], NotUsed] = {
    val hllFlow = new CreateHLLFlow[A]()
    val ffg = Flow.fromGraph(hllFlow)
    val agnt = new HyperLogLogAgentFlow(hllAgent)
    ffg.via(agnt).named("SeqToHLLAgent")
  }

  /** Compose hllFlow & HyperLogLogAgentFlow & Sink
    *
    * @see [[org.gs.algebird.typeclasses.HyperLogLogLike]]
  	*	@tparam A is a HyperLogLogLike with a TypeTag
  	* @param hllAgent Akka Agent accumulates HLL values
  	* @return Sink that accepts Seq[A]
  	*/  
  def compositeSink[A: TypeTag: HyperLogLogLike](hllAgent: HyperLogLogAgent): Sink[Seq[A], NotUsed] = {
    compositeFlow(hllAgent).to(Sink.ignore)
  }  
}
