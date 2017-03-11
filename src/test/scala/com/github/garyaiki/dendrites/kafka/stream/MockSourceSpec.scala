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

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.{ActorAttributes, ActorMaterializer, Graph, SourceShape}
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords}
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import scala.concurrent.Await
import scala.concurrent.duration._
import com.github.garyaiki.dendrites.kafka.MockConsumerConfig

/** Test a Kafka MockConsumer in a Source
   *
   * KafkaSource makes calls to Kafka Java client that block. The default system dispatcher uses
   * Java's ForkJoin thread pool which prefers to allocate as many threads as the CPU has cores.
   * Blocking calls with ForkJoin could possibly cause thread starvation. This can be avoided in
   * AkkaStreams by adding a different dispatcher to a Source, Sink, or Flow. Logging shows which
   * dispatcher is used for each logging call.
  */
class MockSourceSpec extends WordSpecLike {
  implicit val system = ActorSystem("dendrites")
  implicit val ec = system.dispatcher
  implicit val logger = Logging(system, getClass)
  implicit val materializer = ActorMaterializer()
  val mockConsumerFacade = MockConsumerConfig

  "An MockKafkaSource" should {
    "poll ConsumeRecords from Kafka" in {
      val source = KafkaSource[String, String](mockConsumerFacade)
      val future = source.grouped(1).runWith(Sink.head)
      val result = Await.result(future, 1000.millis)
      var crs: ConsumerRecords[String, String] = null
      result match {
        case x: Vector[ConsumerRecords[String, String]] => crs = x(0)
      }
      crs.count() shouldBe 7
    }
    "poll with a blocking dispatcher" in {
      val dispatcher = ActorAttributes.dispatcher("dendrites.blocking-dispatcher")
      val source = KafkaSource[String, String](mockConsumerFacade).withAttributes(dispatcher)
      val future = source.grouped(1).map { elem => logger.debug(elem.toString()); elem}.runWith(Sink.head)
      val result = Await.result(future, 1000.millis)
      var crs: ConsumerRecords[String, String] = null
      result match {
        case x: Vector[ConsumerRecords[String, String]] => crs = x(0)
      }
      crs.count() shouldBe 7
    }
  }
}
