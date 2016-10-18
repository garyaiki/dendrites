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
package org.gs.algebird.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.twitter.algebird.{HLL, HyperLogLogAggregator}
import org.gs.algebird.createHLL
import org.gs.algebird.AlgebirdConfigurer.hyperLogLogBits
import org.gs.algebird.typeclasses.HyperLogLogLike

/** Flow to create HLL from Seq[A]
  *
  * @tparam A: HyperLogLogLike
  * @param bits to initialize HyperLogLogAggregator
  * @author Gary Struthers
  */
class CreateHLLFlow[A: HyperLogLogLike](bits: Int = hyperLogLogBits)
  extends GraphStage[FlowShape[Seq[A], HLL]] {

  val in = Inlet[Seq[A]]("CreateHLLFlow in")
  val out = Outlet[HLL]("CreateHLLFlow out")
  override val shape = FlowShape.of(in, out)
  implicit val ag = HyperLogLogAggregator(bits)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val elem = grab(in)
          push(out, createHLL(elem))
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
