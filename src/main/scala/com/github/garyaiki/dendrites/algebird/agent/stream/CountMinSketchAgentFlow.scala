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
import akka.stream.scaladsl.{Flow, Sink}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.twitter.algebird.{CMS, CMSHasher}
import scala.concurrent.Future
import scala.reflect.runtime.universe.TypeTag
import com.github.garyaiki.dendrites.algebird.{createCMSMonoid, cmsHasherBigDecimal, cmsHasherDouble, cmsHasherFloat}
import com.github.garyaiki.dendrites.algebird.agent.CountMinSketchAgent
import com.github.garyaiki.dendrites.algebird.stream.CreateCMSFlow

/** Flow to update CountMinSketch Agent
  *
  * @deprecated
  * @tparam K: Ordering: CMSHasher
  * @param cmsAgent CountMinSketchAgent
  * @author Gary Struthers
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
        override def onPull(): Unit = pull(in)
      })
    }
  }
}

object CountMinSketchAgentFlow {

  /** Compose cmsFlow & CountMinSketchAgentFlow
    *
    * @tparam A a Numeric that has conversions to CMSHasher with a TypeTag
    * @param cmsAgt Akka Agent accumulates CountMinSketch
    * @return Future for Agents updated value
    */
  def compositeFlow[A: TypeTag: Numeric: CMSHasher](cmsAgt: CountMinSketchAgent[A]):
    Flow[Seq[A], Future[CMS[A]], NotUsed] = {

    val cmsFlow = new CreateCMSFlow[A]()
    val ffg = Flow.fromGraph(cmsFlow)
    val cmsAgtFlow = new CountMinSketchAgentFlow(cmsAgt)
    ffg.via(cmsAgtFlow).named("SeqToCMSAgent")
  }

  /** Compose cmsFlow & CountMinSketchAgentFlow & Sink
    *
    * @tparam A a Numeric that has conversions to CMSHasher with a TypeTag
    * @param cmsAgt Akka Agent accumulates CountMinSketch
    * @return Sink that accepts Seq[A]
    */
  def compositeSink[A: TypeTag: Numeric: CMSHasher](cmsAgt: CountMinSketchAgent[A]): Sink[Seq[A], NotUsed] = {
    compositeFlow(cmsAgt).to(Sink.ignore)
  }
}
