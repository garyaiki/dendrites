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
package com.github.garyaiki.dendrites.kafka

import akka.actor.ActorSystem
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import java.util.{ArrayList, UUID}
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatest.Matchers._
import scala.collection.immutable.{Iterable, Seq}
import scala.concurrent.duration.MILLISECONDS
import com.github.garyaiki.dendrites.avro.{byteArrayToGenericRecord, ccToByteArray, loadSchema}
import com.github.garyaiki.dendrites.examples.account.GetAccountBalances
import com.github.garyaiki.dendrites.examples.account.avro.genericRecordToGetAccountBalances

/**
  *
  * @author Gary Struthers
  *
  */
class AvroKafkaProducerConsumerSpec extends WordSpecLike with BeforeAndAfterAll {
  implicit val system = ActorSystem("dendrites")
  implicit val logger = Logging(system, getClass)
  val config = ConfigFactory.load()
  val timeout = config.getLong("dendrites.kafka.close-timeout")
  val topic = "avroTest-topic"
  val topics = new ArrayList[String]()
  topics.add(topic)
  val key = UUID.randomUUID.toString
  val schema = loadSchema("getAccountBalances.avsc", "/avro/")
  val gab = GetAccountBalances(1L)
  var producer: KafkaProducer[String, Array[Byte]] = null
  var consumer: KafkaConsumer[String, Array[Byte]] = null

  override def beforeAll() {
    producer = createProducer[String, Array[Byte]]("testAvroKafkaProducer.properties")
    consumer = createConsumer[String, Array[Byte]]("testAvroKafkaConsumer.properties")
    consumer.subscribe(topics)
  }

  "A AvroKafkaProducerConsumer" should {
    "serialize a case class and send a message" in {
      val bytes = ccToByteArray(schema, gab)
      val record = new ProducerRecord[String, Array[Byte]](topic, key, bytes)
      val rm: RecordMetadata = producer.send(record).get()

      rm should not be null
      rm.topic shouldBe topic
    }
    "read the message" in {
      val crs = consumer.poll(timeout)
      crs.count shouldBe 1
      val it = crs.iterator
      var consumerRecord: ConsumerRecord[String, Array[Byte]] = null
      while(it.hasNext) { consumerRecord = it.next }
      val value = consumerRecord.value
      val genericRecord: GenericRecord = byteArrayToGenericRecord(schema, value)
      val accountBalances = genericRecordToGetAccountBalances(genericRecord)

      accountBalances shouldBe gab
    }
  }

  override def afterAll() {
    consumer.commitSync()
    consumer.close()
    producer.flush()
    producer.close(timeout, MILLISECONDS)
  }
}
