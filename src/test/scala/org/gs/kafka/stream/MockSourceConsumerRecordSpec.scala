package org.gs.kafka.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Keep, Sink, Source }
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import java.util.{ List => JList }
import org.apache.kafka.clients.consumer.{ ConsumerRecord, ConsumerRecords }
import org.scalatest.{ WordSpecLike, Matchers }
import org.scalatest._
import org.scalatest.Matchers._
import scala.collection.immutable.Queue
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import org.gs.kafka.stream._
import org.gs.kafka.{MockConsumerConfig, MockConsumerRecords}

/** Test a Flow that maps mock ConsumerRecords from a Kafka MockConsumer Source to a queue of
  * ConsumerRecord
  *
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
      assert(result.length === 7)
      val (cr0, q0Tail) = result.dequeue
      assert(cr0.key() === this.mockConsumerFacade.key)
      assert(cr0.offset() === 0L)
      assert(cr0.partition() === 0)
      assert(cr0.topic() === this.mockConsumerFacade.topic)
      assert(cr0.value() === "0")
      val (cr1, q1Tail) = q0Tail.dequeue
      assert(cr1.offset() === 10L)
      assert(cr1.partition() === 0)
      assert(cr1.topic() === this.mockConsumerFacade.topic)
      assert(cr1.value() === "10")
      val (cr2, q2Tail) = q1Tail.dequeue
      assert(cr2.offset() === 20L)
      assert(cr2.partition() === 0)
      assert(cr2.topic() === this.mockConsumerFacade.topic)
      assert(cr2.value() === "20")
      val (cr3, q3Tail) = q2Tail.dequeue
      assert(cr3.offset() === 0L)
      assert(cr3.partition() === 1)
      assert(cr3.topic() === this.mockConsumerFacade.topic)
      assert(cr3.value() === "5")
      val (cr4, q4Tail) = q3Tail.dequeue
      assert(cr4.offset() === 10L)
      assert(cr4.partition() === 1)
      assert(cr4.topic() === this.mockConsumerFacade.topic)
      assert(cr4.value() === "15")
      val (cr5, q5Tail) = q4Tail.dequeue
      assert(cr5.offset() === 20L)
      assert(cr5.partition() === 1)
      assert(cr5.topic() === this.mockConsumerFacade.topic)
      assert(cr5.value() === "25")
      val (cr6, q6Tail) = q5Tail.dequeue
      assert(cr6.offset() === 30L)
      assert(cr6.partition() === 1)
      assert(cr6.topic() === this.mockConsumerFacade.topic)
      assert(cr6.value() === "35")
      intercept[java.util.NoSuchElementException] {
        q6Tail.dequeue
      }
      assert(q6Tail.length === 0)
    }
  }
}
