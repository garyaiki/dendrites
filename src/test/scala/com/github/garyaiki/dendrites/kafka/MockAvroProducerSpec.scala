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
package com.github.garyaiki.dendrites.kafka

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import java.util.concurrent.{Future => JFuture}
import org.apache.kafka.clients.producer.{Callback, MockProducer, ProducerRecord, RecordMetadata}
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.Matchers._
import com.github.garyaiki.dendrites.avro.{byteArrayToGenericRecord, ccToByteArray, loadSchema}
import com.github.garyaiki.dendrites.examples.account.GetAccountBalances

/** Test a Kafka MockConsumer in a Source
  *
  * @Note MockProducer is a singleton, call producer.clear() before and after tests
  */ 
class MockAvroProducerSpec extends WordSpecLike {
  implicit val system = ActorSystem("dendrites")
  implicit val logger = Logging(system, getClass)

  val schema = loadSchema("getAccountBalances.avsc")
  val gab = GetAccountBalances(1L)

  val mock = MockProducerConfig
  val topic = mock.topic
  val key = mock.key


  "A MockAvroProducer" should {
    "serialize a case class and send a message" in {
      val producer: MockProducer[String, Array[Byte]] = mock.producer
      producer.clear()
      val bytes = ccToByteArray(schema, gab)
      val record = new ProducerRecord[String, Array[Byte]](topic, key, bytes)
      val kafkaCallback = new Callback() {
        def onCompletion(meta: RecordMetadata, e: Exception): Unit = {
          e should be(null)
          meta should not be(null)
          meta.topic should equal(topic)
			    producer.history().size shouldBe 1
          producer.close()
        }
      }
      producer.send(record, kafkaCallback)
    }
  }
}
