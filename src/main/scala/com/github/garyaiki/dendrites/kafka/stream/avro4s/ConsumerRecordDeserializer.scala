/** Copyright 2016 - 2017 Gary Struthers

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
package com.github.garyaiki.dendrites.kafka.stream.avro4s

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import org.apache.kafka.clients.consumer.ConsumerRecord

/** Maps a byteArray to a case class
  *
  * @tparam A case class or tuple subclass of Product
  * @param f user function maps Array[Byte] to case class
  * @author Gary Struthers
  */
class ConsumerRecordDeserializer[K, V <: Product](f:(Array[Byte]) => V)
    extends GraphStage[FlowShape[ConsumerRecord[K, Array[Byte]], (K, V)]] {

  val in = Inlet[ConsumerRecord[K, Array[Byte]]]("Avro4sConsumerRecordDeserializer.in")
  val out = Outlet[(K, V)]("Avro4sConsumerRecordDeserializer.out")

  override val shape = FlowShape.of(in, out)

  /** Deserialize ConsumerRecord to Key, case class tuple on push
    *
    * @param inheritedAttributes
    * @return case class
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val consumerRecord: ConsumerRecord[K, Array[Byte]] = grab(in)
          val bytes: Array[Byte] = consumerRecord.value
          val caseClass: V = f(bytes)
          push(out, (consumerRecord.key, caseClass))
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
}
