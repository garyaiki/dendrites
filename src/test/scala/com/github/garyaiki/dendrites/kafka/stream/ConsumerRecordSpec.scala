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
package com.github.garyaiki.dendrites.kafka.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.{ActorMaterializer, ClosedShape, FanOutShape2, FanOutShape3, FlowShape, UniformFanOutShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Merge, RunnableGraph, Source, Sink, Unzip, UnzipWith,
  UnzipWith3}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import java.util.{List => JList}
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords}
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.Matchers._
import scala.collection.immutable.Queue
import com.github.garyaiki.dendrites.kafka.MockConsumerRecords

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

  "ConsumerRecords with 2 TopicPartition" should {
    "extract 2 queues of ConsumerRecord" in {
      val (pub, sub) = TestSource.probe[ConsumerRecords[String, String]]
      .via(dualConsumerRecordsFlow[String, String])
      .toMat(TestSink.probe[(Queue[ConsumerRecord[String, String]], Queue[ConsumerRecord[String, String]])])(Keep.both)
      .run()
      sub.request(1)
      pub.sendNext(cRecords1)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      val partition0Queue = response._1
      partition0Queue.size shouldBe 3
      val partition1Queue = response._2
      partition1Queue.size shouldBe 4

      val q1Vals = new StringBuilder()
      partition0Queue foreach( cr => q1Vals.++=(cr.value()))
      q1Vals.toString() shouldBe "01020"

      val q2Vals = new StringBuilder()
      partition1Queue foreach( cr => q2Vals.++=(cr.value()))
      q2Vals.toString() shouldBe "5152535"
    }

    "extract tuple2 of String" in {
      val g2 = RunnableGraph.fromGraph(GraphDSL.create() { implicit b: GraphDSL.Builder[NotUsed] ⇒
        import GraphDSL.Implicits._
        import scala.language.postfixOps
        val uz = Unzip[String, String]
        val unzip = b.add(uz)
        val source = Source.single("hello")
        val two = Flow.fromFunction[String, (String, String)] { x => (x, x) }
        source ~> two ~> unzip.in

        unzip.out0 ~> Sink.ignore
        unzip.out1 ~> Sink.ignore
        ClosedShape
      }).run()
    }

    "extract tuple2 of queues of ConsumerRecord" in { // @FIXME
      val g2 = RunnableGraph.fromGraph(GraphDSL.create() { implicit b: GraphDSL.Builder[NotUsed] ⇒
        import GraphDSL.Implicits._
        import scala.language.postfixOps
        val dual = b.add(dualConsumerRecordsFlow[String, String])
        val uz = Unzip[Queue[ConsumerRecord[String, String]], Queue[ConsumerRecord[String, String]]]
        val unzip = b.add(uz)
        val source = Source.single(cRecords1)
        source ~> dual ~> unzip.in

        unzip.out0 ~> Sink.ignore
        unzip.out1 ~> Sink.ignore
        ClosedShape
      }).run()
    }

    "extract tuple3 of queues of ConsumerRecord" in {

      val g2 = RunnableGraph.fromGraph(GraphDSL.create() { implicit b: GraphDSL.Builder[NotUsed] ⇒
        import GraphDSL.Implicits._
        import scala.language.postfixOps
        val unzip: FanOutShape3[Int, Int, Int, Int] = b.add(UnzipWith[Int, Int, Int, Int]((b: Int) ⇒ (b, b, b)))

        Source(1 to 10) ~> unzip.in

        unzip.out0 ~> Sink.ignore
        unzip.out1 ~> Sink.ignore
        unzip.out2 ~> Sink.ignore
        ClosedShape
      }).run()
      /*
      val split3Partitions = GraphDSL.create() { implicit b: GraphDSL.Builder[NotUsed] ⇒
        import GraphDSL.Implicits._
        import scala.language.postfixOps
        val unzip: FanOutShape3[Int, Int, Int, Int] = b.add(UnzipWith[Int, Int, Int, Int]((b: Int) ⇒ (b, b, b)))

        Source(1 to 10) ~> unzip.in
        UniformFanOutShape(unzip.in, unzip.out0, unzip.out1, unzip.out2)
      }
*/
    }
  }
}
