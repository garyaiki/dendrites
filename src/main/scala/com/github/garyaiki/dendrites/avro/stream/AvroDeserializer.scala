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
package com.github.garyaiki.dendrites.avro.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import com.github.garyaiki.dendrites.avro.byteArrayToGenericRecord

/** Maps a byteArray first to an Avro GenericRecord, then maps the GenericRecord to a case class
  *
  * @tparam A case class or tuple subclass of Product
  * @param Avro schema for case class
  * @param f user function copies values from Avro GenericRecord to case class
  * @author Gary Struthers
  */
class AvroDeserializer[A <: Product](schema: Schema, f:(GenericRecord) => A)
  extends GraphStage[FlowShape[Array[Byte], A]] {

  val in = Inlet[Array[Byte]]("AvroDeserializer.in")
  val out = Outlet[A]("AvroDeserializer.out")

  override val shape = FlowShape.of(in, out)

  /** Deserialize bytearray to Avro GenericRecord on push
    *
    * @param inheritedAttributes
    * @return GenericRecord
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val bytes = grab(in)
          val record = byteArrayToGenericRecord(schema, bytes)
          push(out, f(record))
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
}
