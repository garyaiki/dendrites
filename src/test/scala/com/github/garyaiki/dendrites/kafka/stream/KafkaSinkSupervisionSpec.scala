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
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.typesafe.config.ConfigFactory
import org.apache.avro.Schema
import org.apache.kafka.clients.producer.{Callback, MockProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.errors.{CorruptRecordException, // Retriable exceptions
  InvalidMetadataException, NetworkException, NotEnoughReplicasAfterAppendException, NotEnoughReplicasException,
  OffsetOutOfRangeException, TimeoutException, UnknownTopicOrPartitionException, RetriableException}
import org.apache.kafka.common.errors.{InvalidTopicException, // Stopping exceptions
  OffsetMetadataTooLarge, RecordBatchTooLargeException, RecordTooLargeException, UnknownServerException}
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.{Iterable, Seq}
import com.github.garyaiki.dendrites.avro.stream.AvroSerializer
import com.github.garyaiki.dendrites.examples.account.GetAccountBalances
import com.github.garyaiki.dendrites.examples.account.avro.genericRecordToGetAccountBalances
import com.github.garyaiki.dendrites.examples.account.avro.AvroGetAccountBalances
import com.github.garyaiki.dendrites.examples.account.kafka.AccountProducer
import com.github.garyaiki.dendrites.kafka.MockProducerConfig

/** Test KafkaSink Supervision. KafkaSink companion object defines a Supervision Decider. Custom
  * AkkaStream stages can use Akka Supervision but they must provide customized ways to handle
  * exception directives returned by the Decider.
  *
  * These tests use a MockKafkaSink so a Kafka server doesn't have to be running, also Kafka's
  * asynchronous callback handler is modified so exceptions can be injected into the callback.
  *
  * Kafka's Retriable exceptions thrown by Kafka Producer are mapped to Supervision.Resume.
  * AkkaStream doesn't have a Retry mode, so Resume is used instead.
  *
  * Retryiable exceptions use exponential backoff and keep retrying until maxDuration is exceeded.
  *
  * Other exceptions thrown by Kafka Producer are mapped to Supervision.Stop. This stops KafkaSink.
  * Each test sends 10 items to a sink. If no exception is injected MockProducer.history shows 10
  * sends. Injecting an exception shows between 1 and 4 sends
  *
  * @Note MockProducer is a singleton, call producer.clear() before and after tests
  * @author Gary Struthers
  *
  */
class KafkaSinkSupervisionSpec extends WordSpecLike with Matchers {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  val config = ConfigFactory.load()
  val closeTimeout = config.getLong("dendrites.kafka.close-timeout")
  val mock = MockProducerConfig
  val avroOps = AvroGetAccountBalances
  val schema: Schema = avroOps.schemaFor(Some("/avro/"), "getAccountBalances.avsc")
  val serializer = new AvroSerializer(schema, avroOps.toBytes)

  val getBals = Seq(GetAccountBalances(0L),
        GetAccountBalances(1L),
        GetAccountBalances(2L),
        GetAccountBalances(3L),
        GetAccountBalances(4L),
        GetAccountBalances(5L),
        GetAccountBalances(6L),
        GetAccountBalances(7L),
        GetAccountBalances(8L),
        GetAccountBalances(9L))

  "A MockKafkaSink" should {
    "serialize case classes and MockSink should send them " in {
      val producer: MockProducer[String, Array[Byte]] = mock.producer
      producer.clear()
      Thread.sleep(400)
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, null)
      source.via(serializer).runWith(sink)
      Thread.sleep(400)
      producer.history().size shouldBe 10
      producer.close()
    }

    "handle CorruptRecordException with Supervision.Resume " in {
      val producer: MockProducer[String, Array[Byte]] = mock.producer
      producer.clear()
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new CorruptRecordException("test"))

      source.via(serializer).runWith(sink)
      Thread.sleep(200)
      producer.history().size shouldBe 3 +- 2
      producer.close()
    }

    "handle UnknownServerException with Supervision.Stop " in {
      val producer: MockProducer[String, Array[Byte]] = mock.producer
      producer.clear()
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new UnknownServerException("test"))

      source.via(serializer).runWith(sink)
      Thread.sleep(200)
      producer.history().size shouldBe 3 +- 2
      producer.close()
    }

    "handle UnknownTopicOrPartitionException with Supervision.Resume " in {
      val producer: MockProducer[String, Array[Byte]] = mock.producer
      producer.clear()
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new UnknownTopicOrPartitionException("test"))

      source.via(serializer).runWith(sink)
      Thread.sleep(200)
      producer.history().size shouldBe 3 +- 2
      producer.close()
    }

    "handle subclass of InvalidMetadataException with Supervision.Resume " in {
      val producer: MockProducer[String, Array[Byte]] = mock.producer
      producer.clear()
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new NetworkException("test"))

      source.via(serializer).runWith(sink)
      Thread.sleep(200)
      producer.history().size shouldBe 3 +- 2
      producer.close()
    }

    "handle NotEnoughReplicasAfterAppendException with Supervision.Resume " in {
      val producer: MockProducer[String, Array[Byte]] = mock.producer
      producer.clear()
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new NotEnoughReplicasAfterAppendException("test"))

      source.via(serializer).runWith(sink)
      Thread.sleep(200)
      producer.history().size shouldBe 3 +- 2
      producer.close()
    }

    "handle NotEnoughReplicasException with Supervision.Resume " in {
      val producer: MockProducer[String, Array[Byte]] = mock.producer
      producer.clear()
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new NotEnoughReplicasAfterAppendException("test"))

      source.via(serializer).runWith(sink)
      Thread.sleep(300)
      producer.history().size shouldBe 3 +- 2
      producer.close()
    }

    "handle OffsetOutOfRangeException with Supervision.Resume " in {
      val producer: MockProducer[String, Array[Byte]] = mock.producer
      producer.clear()
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new OffsetOutOfRangeException("test"))

      source.via(serializer).runWith(sink)
      Thread.sleep(200)
      producer.history().size shouldBe 3 +- 2
      producer.close()
    }

    "handle TimeoutException with Supervision.Resume " in {
      val producer: MockProducer[String, Array[Byte]] = mock.producer
      producer.clear()
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new TimeoutException("test"))

      source.via(serializer).runWith(sink)
      Thread.sleep(200)
      producer.history().size shouldBe 3 +- 2
      producer.close()
    }

    "handle subclass of RetriableException with Supervision.Resume " in {
      val producer: MockProducer[String, Array[Byte]] = mock.producer
      producer.clear()
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new TimeoutException("test"))

      source.via(serializer).runWith(sink)
      Thread.sleep(200)
      producer.history().size shouldBe 4 +- 2
      producer.close()
    }

    "handle InvalidTopicException with Supervision.Stop " in {
      val producer: MockProducer[String, Array[Byte]] = mock.producer
      producer.clear()
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new InvalidTopicException("test"))

      source.via(serializer).runWith(sink)
      Thread.sleep(300)
      producer.history().size shouldBe 3 +- 2
      producer.close()
    }

    "handle OffsetMetadataTooLarge with Supervision.Stop " in {
      val producer: MockProducer[String, Array[Byte]] = mock.producer
      producer.clear()
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new OffsetMetadataTooLarge("test"))

      source.via(serializer).runWith(sink)
      Thread.sleep(300)
      producer.history().size shouldBe 3 +- 2
      producer.close()
    }

    "handle RecordBatchTooLargeException with Supervision.Stop " in {
      val producer: MockProducer[String, Array[Byte]] = mock.producer
      producer.clear()
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new RecordBatchTooLargeException("test"))

      source.via(serializer).runWith(sink)
      Thread.sleep(300)
      producer.history().size shouldBe 3 +- 2
      producer.close()
    }

    "handle RecordTooLargeException with Supervision.Stop " in {
      val producer: MockProducer[String, Array[Byte]] = mock.producer
      producer.clear()
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new RecordTooLargeException("test"))

      source.via(serializer).runWith(sink)
      Thread.sleep(300)
      producer.history().size shouldBe 3 +- 2
      producer.close()
    }

    "handle KafkaException with Supervision.Stop " in {
      val producer: MockProducer[String, Array[Byte]] = mock.producer
      producer.clear()
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new KafkaException("test"))

      source.via(serializer).runWith(sink)
      Thread.sleep(300)
      producer.history().size shouldBe 3 +- 2
      producer.close()
    }
  }
}
