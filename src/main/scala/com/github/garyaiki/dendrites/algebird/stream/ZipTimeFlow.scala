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
package com.github.garyaiki.dendrites.algebird.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

/** Flow that generates elem/time tuples from elem. Meant for DecayedValues
  *
  * @tparam A Numeric
  * @param f produces time, arg:A is ignore-able
  * @example [[com.github.garyaiki.dendrites.algebird.agent.stream.DecayedValueAgentFlow]]
  * @author Gary Struthers
  */
class ZipTimeFlow[A: Numeric](f: A => Double)
        extends GraphStage[FlowShape[Seq[A], Seq[(Double, Double)]]] {

  val in = Inlet[Seq[A]]("ZipTimeFlow in")
  val out = Outlet[Seq[(Double, Double)]]("ZipTimeFlow out")
  override val shape = FlowShape.of(in, out)

  def toZipTime(xs: Seq[A]): Seq[(Double, Double)] =
    xs.map(x => (x.asInstanceOf[Number].doubleValue(), f(x)))// Instant.now.toEpochMilli.toDouble))

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val elem = grab(in)
          push(out, toZipTime(elem))
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
