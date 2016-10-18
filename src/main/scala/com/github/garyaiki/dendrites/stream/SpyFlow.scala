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
package org.gs.stream

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

/** Flow that does nothing but expose pull count, push count, and message pushed. Use for debugging
  *
  * @tparam A input and output type
  * @param pulls count of onPull
  * @param pushes count of onPushes
  * @author Gary Struthers
  */
class SpyFlow[A](name: String, var pulls: Int, var pushes: Int)(implicit logger: LoggingAdapter)
        extends GraphStage[FlowShape[A, A]] {

  logger.debug("spyFlow:{} init pulls:{} pushes{}", name, pulls, pushes)

  val in = Inlet[A]("MonitorStream in")
  val out = Outlet[A]("MonitorStream out")
  override val shape = FlowShape.of(in, out)
  var elem: A = null.asInstanceOf[A]
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          elem = grab(in)
          pushes += 1
          logger.debug("spyFlow:{} onPush count:{} elem{}", name, pushes, elem)
          push(out, elem)
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pulls += 1
          logger.debug("spyFlow:{} onPull count:{}", name, pulls)
          pull(in)
        }
      })
    }
  }
}

