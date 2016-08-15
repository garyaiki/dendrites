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
import akka.event.{LoggingAdapter, Logging}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep,Flow, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{Callback, MockProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.errors.{CorruptRecordException, // Retriable exceptions
  InvalidMetadataException,
  NetworkException,
  NotEnoughReplicasAfterAppendException,
  NotEnoughReplicasException,
  OffsetOutOfRangeException,
  TimeoutException,
  UnknownTopicOrPartitionException,
  RetriableException}
import org.apache.kafka.common.errors.{InvalidTopicException, //Stopping exceptions
  OffsetMetadataTooLarge,
  RecordBatchTooLargeException,
  RecordTooLargeException,
  UnknownServerException}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.{Iterable, Seq}
//import scala.concurrent.duration._
import org.gs.avro.ccToByteArray
import org.gs.avro.stream.{AvroDeserializer, AvroSerializer}
import org.gs.examples.account.GetAccountBalances
import org.gs.examples.account.avro.genericRecordToGetAccountBalances
import org.gs.examples.account.kafka.{AccountConsumer, AccountProducer}
import org.gs.kafka.MockProducerConfig
import KafkaSource.decider

/** Test KafkaSink Supervision. KafkaSink companion object defines a Supervision Decider. Custom
  * AkkaStream stages can use Akka Supervision but they must provide customized ways to handle
  * exception directives returned by the Decider.
  *
  * These tests use a MockKafkaSink so a Kafka server doesn't have to be running, also Kafka's
  * asynchronous callback handler is modified so exceptions can be injected into the callback. 
  *
  * Kafka's Retriable exceptions thrown by Kafka Producer are mapped to Supervision.Resume.
  * AkkaStream doesn't have a Retry mode, so Resume is used instead.A Producer.send that failed with
  * a Retriable exception will retry the send and will keep retrying until there is a Stop exception
  * or the stream times out.
  *
  * The real KafkaSink keeps retrying as long as Retryiable exceptions are thrown. MockKafkaSink
  * only injects an exception once. 
  *
  * Other exceptions thrown by Kafka Producer are mapped to Supervision.Stop. This stops KafkaSink.
  * Each test sends 10 items to a sink. If no exception is injected MockProducer.history shows 10
  * sends. Injecting a Retryable exception produces 11 sends. Injecting a Stop exception sends 1.
  *
  * @author Gary Struthers
  *
  */
class KafkaSinkSupervisionSpec extends WordSpecLike with Matchers {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  val config = ConfigFactory.load()
  val closeTimeout = config.getLong("dendrites.kafka.account.close-timeout")
  val mock = MockProducerConfig
  val producer: MockProducer[String, Array[Byte]] = mock.producer
  val serializer = new AvroSerializer("getAccountBalances.avsc", ccToByteArray)

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
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, null)
      source.via(serializer).runWith(sink)
      Thread.sleep(5000)
			val history = producer.history()
      eventually (timeout(3 seconds), interval(500 millis)) { history.size should be (10)  }
      producer.clear()
    }

    "handle CorruptRecordException with Supervision.Resume " in {
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new CorruptRecordException("test"))
          
      source.via(serializer).runWith(sink)
      Thread.sleep(5000)
			val history = producer.history()
      eventually (timeout(3 seconds), interval(500 millis)) { history.size should be (11)  }
      producer.clear()
    }

    "handle UnknownServerException with Supervision.Stop " in {
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new UnknownServerException("test"))
          
      source.via(serializer).runWith(sink)
      Thread.sleep(5000)
			val history = producer.history()
      eventually (timeout(3 seconds), interval(500 millis)) { history.size should be (1)  }
      producer.clear()
    }

    "handle UnknownTopicOrPartitionException with Supervision.Resume " in {
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new UnknownTopicOrPartitionException("test"))
          
      source.via(serializer).runWith(sink)
      Thread.sleep(5000)
			val history = producer.history()
      eventually (timeout(3 seconds), interval(500 millis)) { history.size should be (11)  }
      producer.clear()
    }

    "handle subclass of InvalidMetadataException with Supervision.Resume " in {
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new NetworkException("test"))
          
      source.via(serializer).runWith(sink)
      Thread.sleep(5000)
			val history = producer.history()
      eventually (timeout(3 seconds), interval(500 millis)) { history.size should be (11)  }
      producer.clear()
    }

    "handle NotEnoughReplicasAfterAppendException with Supervision.Resume " in {
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new NotEnoughReplicasAfterAppendException("test"))
          
      source.via(serializer).runWith(sink)
      Thread.sleep(5000)
			val history = producer.history()
      eventually (timeout(3 seconds), interval(500 millis)) { history.size should be (11)  }
      producer.clear()
    }

    "handle NotEnoughReplicasException with Supervision.Resume " in {
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new NotEnoughReplicasAfterAppendException("test"))
          
      source.via(serializer).runWith(sink)
      Thread.sleep(5000)
			val history = producer.history()
      eventually (timeout(3 seconds), interval(500 millis)) { history.size should be (11)  }
      producer.clear()
    }

    "handle OffsetOutOfRangeException with Supervision.Resume " in {
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new OffsetOutOfRangeException("test"))
          
      source.via(serializer).runWith(sink)
      Thread.sleep(5000)
			val history = producer.history()
      eventually (timeout(3 seconds), interval(500 millis)) { history.size should be (11)  }
      producer.clear()
    }

    "handle TimeoutException with Supervision.Resume " in {
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new TimeoutException("test"))
          
      source.via(serializer).runWith(sink)
      Thread.sleep(5000)
			val history = producer.history()
      eventually (timeout(3 seconds), interval(500 millis)) { history.size should be (11)  }
      producer.clear()
    }

    "handle subclass of RetriableException with Supervision.Resume " in {
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new TimeoutException("test"))
          
      source.via(serializer).runWith(sink)
      Thread.sleep(5000)
			val history = producer.history()
      eventually (timeout(3 seconds), interval(500 millis)) { history.size should be (11)  }
      producer.clear()
    }

    "handle InvalidTopicException with Supervision.Stop " in {
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new InvalidTopicException("test"))
          
      source.via(serializer).runWith(sink)
      Thread.sleep(5000)
			val history = producer.history()
      eventually (timeout(3 seconds), interval(500 millis)) { history.size should be (1)  }
      producer.clear()
    }

    "handle OffsetMetadataTooLarge with Supervision.Stop " in {
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new OffsetMetadataTooLarge("test"))
          
      source.via(serializer).runWith(sink)
      Thread.sleep(5000)
			val history = producer.history()
      eventually (timeout(3 seconds), interval(500 millis)) { history.size should be (1)  }
      producer.clear()
    }

    "handle RecordBatchTooLargeException with Supervision.Stop " in {
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new RecordBatchTooLargeException("test"))
          
      source.via(serializer).runWith(sink)
      Thread.sleep(5000)
			val history = producer.history()
      eventually (timeout(3 seconds), interval(500 millis)) { history.size should be (1)  }
      producer.clear()
    }

    "handle RecordTooLargeException with Supervision.Stop " in {
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new RecordTooLargeException("test"))
          
      source.via(serializer).runWith(sink)
      Thread.sleep(5000)
			val history = producer.history()
      eventually (timeout(3 seconds), interval(500 millis)) { history.size should be (1)  }
      producer.clear()
    }

    "handle KafkaException with Supervision.Stop " in {
      val iter = Iterable(getBals.toSeq:_*)
      val source = Source[GetAccountBalances](iter)
      val sink = MockKafkaSink[String, Array[Byte]](mock, new KafkaException("test"))
          
      source.via(serializer).runWith(sink)
      Thread.sleep(5000)
			val history = producer.history()
      eventually (timeout(3 seconds), interval(500 millis)) { history.size should be (1)  }
      producer.clear()
    }
  }
}
