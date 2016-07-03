package org.gs.kafka.stream

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import java.util.{List => JList}
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords}
import org.scalatest.{WordSpecLike, Matchers}
import org.scalatest.Matchers._
import scala.collection.immutable.Queue
import org.gs.kafka.MockConsumerRecords

class ConsumerRecordSpec extends WordSpecLike with MockConsumerRecords {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  
  "ConsumerRecords with 1 TopicPartition" should {
    "extract a queue of ConsumerRecord" in {
      val (pub, sub) = TestSource.probe[ConsumerRecords[String, String]]
      .via(consumerRecordsFlow[String, String])
      .toMat(TestSink.probe[Queue[ConsumerRecord[String, String]]])(Keep.both)
      .run()
      sub.request(1)
      pub.sendNext(cRecords0)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      response.size shouldBe 3
      val (cr0, q1) = response.dequeue

      q1.size shouldBe 2
      cr0.value() shouldBe "0"

      val (cr1, q2) = q1.dequeue
      q2.size shouldBe 1
      cr1.value() shouldBe "10"
      val (cr2, q3) = q2.dequeue
      q3.size shouldBe 0
      cr2.value() shouldBe "20"
    }
  }
  
  "ConsumerRecords with 2 TopicPartition2" should {
    "extract 2 queues of ConsumerRecord" in {
      val (pub, sub) = TestSource.probe[ConsumerRecords[String, String]]
      .via(consumerRecordsFlow[String, String])
      .toMat(TestSink.probe[Queue[ConsumerRecord[String, String]]])(Keep.both)
      .run()
      sub.request(1)
      pub.sendNext(cRecords0)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      response.size shouldBe 3

      val (cr0, q1) = response.dequeue
      q1.size shouldBe 2
      cr0.value() shouldBe "0"

      val (cr1, q2) = q1.dequeue
      q2.size shouldBe 1
      cr1.value() shouldBe "10"

      val (cr2, q3) = q2.dequeue
      q3.size shouldBe 0
      cr2.value() shouldBe "20"
    }
  }
}
