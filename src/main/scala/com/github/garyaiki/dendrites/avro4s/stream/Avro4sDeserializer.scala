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

/** Maps a byteArray to a case class
  *
  * @tparam A case class or tuple subclass of Product
  * @param f user function maps Array[Byte] to case class
  * @author Gary Struthers
  */
class Avro4sDeserializer[A <: Product](f:(Array[Byte]) => A)
    extends GraphStage[FlowShape[Array[Byte], A]] {

  val in = Inlet[Array[Byte]]("Avro4sSerializer.in")
  val out = Outlet[A]("Avro4sSerializer.out")

  override val shape = FlowShape.of(in, out)

  /** Deserialize bytearray to case class on push
    *
    * @param inheritedAttributes
    * @return case class
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val bytes = grab(in)
          push(out, f(bytes))
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
}
