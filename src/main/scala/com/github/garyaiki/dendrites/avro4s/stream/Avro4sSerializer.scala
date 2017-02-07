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
package com.github.garyaiki.dendrites.avro4s.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

/** Flow serializes a case class. Pass case class to f, push serialized object
  *
  * @tparam A case classes and tuples are subtypes of Produce
  * @param serialization function
  *
  * @author Gary Struthers
  */
class Avro4sSerializer[A <: Product](f:(A) => Array[Byte]) extends GraphStage[FlowShape[A, Array[Byte]]] {
  val in = Inlet[A]("Avro4sSerializer.in")
  val out = Outlet[Array[Byte]]("Avro4sSerializer.out")

  override val shape = FlowShape.of(in, out)
  override def createLogic(attr: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          push(out, f(grab(in)))
        }
      })
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
}
