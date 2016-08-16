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
package org.gs.kafka.stream

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import java.util.{List => JList}
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords}
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.Matchers._
import scala.collection.immutable.Queue
import org.gs.kafka.{MockConsumerConfig, MockConsumerRecords}

/** Test a Flow that maps mock ConsumerRecords from a Kafka MockConsumer Source to a queue of
  * ConsumerRecord
  *
  * @author Gary Struthers
  */ 
class MockSourceConsumerRecordSpec extends WordSpecLike {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val mockConsumerFacade = MockConsumerConfig
  val source = KafkaSource[String, String](mockConsumerFacade)

  "ConsumerRecords with 2 TopicPartitions" should {
    "extract a queue of ConsumerRecord" in {
      val sourceUnderTest = source.via(consumerRecordsFlow[String, String])
      val result = sourceUnderTest
        .runWith(TestSink.probe[Queue[ConsumerRecord[String, String]]])
        .request(1)
        .expectNext()

      result.length shouldBe 7
      val (cr0, q0Tail) = result.dequeue
      cr0.key() shouldBe this.mockConsumerFacade.key
      cr0.offset() shouldBe 0L
      cr0.partition() shouldBe 0
      cr0.topic() shouldBe this.mockConsumerFacade.topic
      cr0.value() shouldBe "0"

      val (cr1, q1Tail) = q0Tail.dequeue
      cr1.offset() shouldBe 10L
      cr1.partition() shouldBe 0
      cr1.topic() shouldBe this.mockConsumerFacade.topic
      cr1.value() shouldBe "10"

      val (cr2, q2Tail) = q1Tail.dequeue
      cr2.offset() shouldBe 20L
      cr2.partition() shouldBe 0
      cr2.topic() shouldBe this.mockConsumerFacade.topic
      cr2.value() shouldBe "20"

      val (cr3, q3Tail) = q2Tail.dequeue
      cr3.offset() shouldBe 0L
      cr3.partition() shouldBe 1
      cr3.topic() shouldBe this.mockConsumerFacade.topic
      cr3.value() shouldBe "5"

      val (cr4, q4Tail) = q3Tail.dequeue
      cr4.offset() shouldBe 10L
      cr4.partition() shouldBe 1
      cr4.topic() shouldBe this.mockConsumerFacade.topic
      cr4.value() shouldBe "15"

      val (cr5, q5Tail) = q4Tail.dequeue
      cr5.offset() shouldBe 20L
      cr5.partition() shouldBe 1
      cr5.topic() shouldBe this.mockConsumerFacade.topic
      cr5.value() shouldBe "25"

      val (cr6, q6Tail) = q5Tail.dequeue
      cr6.offset() shouldBe 30L
      cr6.partition() shouldBe 1
      cr6.topic() shouldBe this.mockConsumerFacade.topic
      cr6.value() shouldBe "35"
      intercept[java.util.NoSuchElementException] { q6Tail.dequeue }
      q6Tail.length shouldBe 0
    }
  }
}
