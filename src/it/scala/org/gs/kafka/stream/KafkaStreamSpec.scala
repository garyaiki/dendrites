/**
  */
package org.gs.kafka.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.ConfigFactory
import org.scalatest._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import scala.collection.immutable.Iterable
import scala.concurrent.duration._
import org.gs._
import org.gs.avro._
import org.gs.avro.stream.{AvroDeserializer, AvroSerializer}
import org.gs.examples.account.GetAccountBalances
import org.gs.examples.account.avro._
import org.gs.examples.account.kafka.{AccountConsumerConfig, AccountProducer}

/** Test integration of Kafka with Akka Streams. First a stream serializes case classes to Avro then
  * writes them to Kafka. Then another stream reads from Kafka and deserializes them back to case
  * classes. 
  *
  * AvroSerializer takes an Avro schema for the case class and a function that produces a byte array
  * If the case class only has simple types the ccToByteArray function will work. Case classes with
  * complex fields needs a custom function like ccToByteArray.
  *
  * KafkaSink is initialized with a wrapped KafkaProducer. The wrapper includes topic, key, and Key,
  * Value types specific to this topic. KafkaProducer is heavy weight and multi-threaded and usually
  * serves other topics and is long lived. If a Kafka RetryableException is thrown while writing
  * KafkaSink catches it and retries the write. If a write throws a subclass of KafkaException this
  * test's Decider stops the write stream.
  *
  * KafkaSource calls KafkaConsumer.poll() which reads all available messages into a ConsumerRecords
  * if it's not empty it's pushed to the next stage. KafkaSource receives an onPull when the stream
  * starts and when all messages in the last poll() have been processed. This uses KafkaConsumer's
  * commitSync after all messages from the last poll() have been processed. This adds resilliency.
  * Kafka commitSync was meant to confirmed that messages have been read. But in an Akka Stream it's
  * purpose can expand to confirm all messages in the stream have been processed. A thrown exception
  * or a timeout means commitSync won't be called. This means the messages that weren't committed
  * will be retried after the next poll().
  *
  * @author Gary Struthers
  *
  */
class KafkaStreamSpec extends WordSpecLike with Matchers with BeforeAndAfterAll {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val config = ConfigFactory.load()
  //val futureTimeout = Timeout(3000.millis)
  val timeout = config.getLong("dendrites.kafka.account.close-timeout")
  val ap = AccountProducer
  val accountConsumerConfig = AccountConsumerConfig
  override def beforeAll() {
    
  }
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
  val iter = Iterable(getBals.toSeq:_*)
  "An KafkaStream" should {
    "serialize case classes then write them to Kafka" in {

      val serializer = new AvroSerializer("getAccountBalances.avsc", ccToByteArray)
      val sink = KafkaSink[String, Array[Byte]](ap)
      val source = Source[GetAccountBalances](iter)
      source.via(serializer).runWith(sink)

      val kafkaSource = KafkaSource[String, Array[Byte]](accountConsumerConfig)
      val consumerRecordQueue = new ConsumerRecordQueue[String, Array[Byte]]()
      val deserializer = new AvroDeserializer("getAccountBalances.avsc",
            genericRecordToGetAccountBalances)
      val streamFuture = kafkaSource
          .via(consumerRecordsFlow[String, Array[Byte]])
          .via(consumerRecordQueue)
          .via(consumerRecordValueFlow)
          .via(deserializer)
          .runWith(Sink.fold(0){(sum, _) => sum + 1})
    }
  }

  override def afterAll() {
    ap.producer.flush()
    Thread.sleep(timeout)
    ap.producer.close(timeout * 10, scala.concurrent.duration.MILLISECONDS)   
  }
}
