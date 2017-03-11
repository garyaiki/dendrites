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
package com.github.garyaiki.dendrites.algebird.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.twitter.algebird.{CMS, CMSHasher, CMSMonoid}
import com.github.garyaiki.dendrites.algebird.{createCMSMonoid, createCountMinSketch}

/** Flow to create CMS from Seq[K]
  *
  * @tparam K: Ordering: CMSHasher
  * @author Gary Struthers
  */
class CreateCMSFlow[K: Ordering: CMSHasher] extends GraphStage[FlowShape[Seq[K], CMS[K]]] {

  val in = Inlet[Seq[K]]("K => CMS in")
  val out = Outlet[CMS[K]]("CMS out")
  override val shape = FlowShape.of(in, out)

  implicit val monoid: CMSMonoid[K] = createCMSMonoid[K]()

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val elem = grab(in)
          push(out, createCountMinSketch(elem))
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
