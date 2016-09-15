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
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import java.util.{List => JList}
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords}
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.Matchers._
import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext
import org.gs.kafka.{MockConsumerConfig, MockConsumerRecords}
import org.gs.stream.SpyFlow

/** Test a Flow that maps mock ConsumerRecords from a Kafka MockConsumer Source to a queue of
  * ConsumerRecord
  *
  * @author Gary Struthers
  */ 
class ResilientStreamSpec extends WordSpecLike {
  implicit val system = ActorSystem("dendrites")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val mockConsumerFacade = MockConsumerConfig
  val source = KafkaSource[String, String](mockConsumerFacade)
  val consumerRecordQueue = new ConsumerRecordQueue[String, String]()


  val mockVals = Seq("0","5","10","15","20","25","35")
  val mockPartitionVals = Seq("0","10","20","5","15","25","35")

  val expect6 = mockVals.take(6)

  "KafkaSource stream" should {
    "pull once from source for 1 poll" in {
      val spy = new SpyFlow[ConsumerRecords[String,String]]("test 1 spy 1", 0, 0)
      val sourceUnderTest = source
        .via(spy)
        .via(consumerRecordsFlow[String, String])
        .via(consumerRecordQueue)
        .via(consumerRecordValueFlow)
      val result = sourceUnderTest
        .runWith(TestSink.probe[String])
        .request(7)
        .expectNextUnorderedN(mockVals)
      spy.pulls shouldBe 1
      spy.pushes shouldBe 1
    }
    "pull again from source after draining stream" in {
      val spy = new SpyFlow[ConsumerRecords[String,String]]("test 2 spy 1", 0, 0)
      val spy2 = new SpyFlow[String]("test 2 spy 2", 0, 0)
      val sourceUnderTest = source
      .via(spy)
      .via(consumerRecordsFlow[String, String])
      .via(consumerRecordQueue)
      .via(consumerRecordValueFlow)
      .via(spy2)
      val result = sourceUnderTest
        .runWith(TestSink.probe[String])
        .request(8)
        .expectNextUnorderedN(mockVals)
      spy.pulls shouldBe 1
      spy.pushes shouldBe 1
      spy2.pulls shouldBe 8
      spy2.pushes shouldBe 7
    }
    "pull once from source after exception" in {
      val spy = new SpyFlow[ConsumerRecords[String,String]]("test 3 spy 1", 0, 0)
      val spy2 = new SpyFlow[String]("test 3 spy 2", 0, 0)
      val sourceUnderTest = source
        .via(spy)
        .via(consumerRecordsFlow[String, String])
        .via(consumerRecordQueue)
        .via(consumerRecordValueFlow)
        .via(spy2)
        .map{elem => if(elem == "35") throw new NullPointerException(s"elem:$elem") else elem}
      val result = sourceUnderTest
        .runWith(TestSink.probe[String])
        .request(8)
        .expectNext(mockPartitionVals(0))
        .expectNext(mockPartitionVals(1))
        .expectNext(mockPartitionVals(2))
        .expectNext(mockPartitionVals(3))
        .expectNext(mockPartitionVals(4))
        .expectNext(mockPartitionVals(5))
        .expectNextOrError()
      spy.pulls shouldBe 1
      spy.pushes shouldBe 1
      spy2.pulls shouldBe 7
      spy2.pushes shouldBe 7
      result.isLeft should be (true)
    }
  }
}
