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
import akka.stream.{ActorMaterializer, Graph, SourceShape}
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords}
import org.apache.kafka.common.TopicPartition
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import org.gs.kafka.MockConsumerConfig

/** Test a Kafka MockConsumer in a Source */ 
class MockSourceSpec extends WordSpecLike {
  implicit val system = ActorSystem("dendrites")
  implicit val logger = Logging(system, getClass)
  implicit val materializer = ActorMaterializer()
  val mockConsumerFacade = MockConsumerConfig

  val source = KafkaSource[String, String](mockConsumerFacade)
        
  "An MockKafkaSource" should {
    "poll ConsumeRecords from Kafka" in {
      val future = source.grouped(1).runWith(Sink.head)
      val result = Await.result(future, 1000.millis)
      var crs: ConsumerRecords[String, String] = null
      result match {
        case x: Vector[ConsumerRecords[String, String]] => crs = x(0)
      }
      crs.count() shouldBe 7
    }
  }
}
