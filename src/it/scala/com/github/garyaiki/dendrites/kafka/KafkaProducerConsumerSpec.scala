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
import java.util.ArrayList
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatest.Matchers._
import scala.collection.immutable.{Iterable, Seq}
import scala.concurrent.duration.MILLISECONDS

/**
  *
  * @author Gary Struthers
  *
  */
class KafkaProducerConsumerSpec extends WordSpecLike with BeforeAndAfterAll {
  implicit val system = ActorSystem("dendrites")
  implicit val logger = Logging(system, getClass)
  val config = ConfigFactory.load()
  val timeout = config.getLong("dendrites.kafka.close-timeout")
  val topic = "test-topic"
  val topics = new ArrayList[String]()
  topics.add(topic)
  val key = "test-key"
  val value = "test-value"
  var producer: KafkaProducer[String, String] = null
  var consumer: KafkaConsumer[String, String] = null

  override def beforeAll() {
    producer = createProducer[String, String]("testKafkaProducer.properties")
    consumer = createConsumer[String, String]("testKafkaConsumer.properties")
    consumer.subscribe(topics)
  }

  "A KafkaProducerConsumer" should {
    "send a message" in {
      val record = new ProducerRecord[String, String](topic, key, value)
      val rm: RecordMetadata = producer.send(record).get()

      rm should not be null
      rm.topic shouldBe topic
    }
    "read the message" in {
      val crs = consumer.poll(timeout)
      assert(crs.count === 1)
      val it = crs.iterator
      while(it.hasNext) {
        val cr = it.next
        assert(cr.key === key)
        assert(cr.value === value)
      }
    }
  }

  override def afterAll() {
    consumer.commitSync()
    consumer.close()
    producer.flush()
    producer.close(timeout, MILLISECONDS)
  }
}
