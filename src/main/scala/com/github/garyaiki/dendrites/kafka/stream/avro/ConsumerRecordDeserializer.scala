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
package com.github.garyaiki.dendrites.kafka.stream.avro

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import com.github.garyaiki.dendrites.avro.byteArrayToGenericRecord
import com.github.garyaiki.dendrites.kafka.ConsumerRecordMetadata

/** Maps a ConsumerRecord to a tuple of key, case class. This differs from AvroDeserializer in that it forwards the
  * Kafka key. Use this when you want to deduplicate keys which can happen when there was an error processing Kafka
  * messages.
  *
  * @tparam K Kafka ConsumerRecord Key
  * @tparam V case class
  * @param Avro schema for case class
  * @param f user function copies values from Avro GenericRecord to case class
  * @author Gary Struthers
  */
class ConsumerRecordDeserializer[K, V <: Product](schema: Schema, f:(GenericRecord) => V)
  extends GraphStage[FlowShape[ConsumerRecord[K, Array[Byte]], (ConsumerRecordMetadata[K], V)]] {

  val in = Inlet[ConsumerRecord[K, Array[Byte]]]("ConsumerRecordDeserializer.in")
  val out = Outlet[(ConsumerRecordMetadata[K], V)]("ConsumerRecordDeserializer.out")

  override val shape = FlowShape.of(in, out)

  /** deserializing message via Avro GenericRecord
    *
    * @param bytes
    * @return case class or tuple
    */
  def mapVal(bytes: Array[Byte]): V = {
    val genericRecord = byteArrayToGenericRecord(schema, bytes)
    f(genericRecord)
  }

  /** Deserialize ConsumerRecord to ConsumerRecordMetadata, case class tuple on push
    *
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val consumerRecord: ConsumerRecord[K, Array[Byte]] = grab(in)
          push(out, (ConsumerRecordMetadata(consumerRecord), mapVal(consumerRecord.value)))
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = pull(in)
      })
    }
}
