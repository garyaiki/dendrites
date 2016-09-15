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
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import org.apache.kafka.clients.consumer.{CommitFailedException,
      InvalidOffsetException,
      OffsetOutOfRangeException}
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.errors.{AuthorizationException, WakeupException}
import org.scalatest.{Matchers, WordSpecLike}
import scala.collection.immutable.Seq
import KafkaSource.decider

/** Test KafkaSource Supervision. KafkaSource companion object defines a Supervision Decider. Custom
  * AkkaStream stages can use Akka Supervision but they must provide customized ways to handle
  * exception directives returned by the Decider.
  *
  * These tests use a MockKafkaSource so a Kafka server doesn't have to be running, also Kafka's
  * asynchronous callback handler is modified so exceptions can be injected into the callback. 
  *
  * Kafka's Retriable exceptions thrown by Kafka Producer are mapped to Supervision.Resume.
  * AkkaStream doesn't have a Retry mode, so Resume is used instead.A Consumer.poll or
  * Consumer.commit that failed with a Retriable exception will retry as many times as the retries
  * variable or until there is a Stop exception. 
  *
  * Other exceptions thrown by Kafka Consumer are mapped to Supervision.Stop. This stops KafkaSource.
  * Each test sends 10 items to a sink. If no exception is injected TestSink.Probe.expectNextN
  * should have all elems handled by the Source. Injecting either a Retryable or a Stop exception
  * TestSink.Probe.expectError should be the injected exception.
  *
  * @author Gary Struthers
  *
  */
class KafkaSourceSupervisionSpec extends WordSpecLike with Matchers {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  val nums = Seq("0","1","2","3","4","5","6","7","8","9")

  "A MockKafkaSource" should {
    "output all input elements when no exception injected" in {
      val iter: Iterator[String] = nums.toIterator
      val source = MockKafkaSource[String](iter)
      val countFuture = source.runWith(TestSink.probe[String])
      .request(10)
      .expectNextN(nums)
    }

    "retry CommitFailedException until > maxBackoff" in {
      val iter: Iterator[String] = nums.toIterator
      val source = MockKafkaSource[String](iter, new CommitFailedException("test"))
      val error = source.runWith(TestSink.probe[String])
      .request(10)
      .expectError()
      Thread.sleep(200) //Wait for retries
      error shouldBe a [CommitFailedException]
    }

    "retry WakeupException until > maxBackoff" in {
      val iter: Iterator[String] = nums.toIterator
      val source = MockKafkaSource[String](iter, new WakeupException())
      val error = source.runWith(TestSink.probe[String])
      .request(10)
      .expectError()
      Thread.sleep(200) //Wait for retries
      error shouldBe a [WakeupException]
    }

    "Stop on AuthorizationException" in {
      val iter: Iterator[String] = nums.toIterator
      val source = MockKafkaSource[String](iter, new AuthorizationException("test"))
      val error = source.runWith(TestSink.probe[String])
      .request(10)
      .expectError()
      error shouldBe a [AuthorizationException]
    }

    "Stop on subclass of InvalidOffsetException" in {
      val iter: Iterator[String] = nums.toIterator
      val source = MockKafkaSource[String](iter, new OffsetOutOfRangeException(null))
      val error = source.runWith(TestSink.probe[String])
      .request(10)
      .expectError()
      error shouldBe a [OffsetOutOfRangeException]
    }

    "Stop on KafkaException" in {
      val iter: Iterator[String] = nums.toIterator
      val source = MockKafkaSource[String](iter, new KafkaException("test"))
      val error = source.runWith(TestSink.probe[String])
      .request(10)
      .expectError()
      error shouldBe a [KafkaException]
    }
  }
}
