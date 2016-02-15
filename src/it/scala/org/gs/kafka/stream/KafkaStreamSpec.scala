/**
  */
package org.gs.kafka.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import org.apache.avro.generic.{GenericDatumReader, GenericRecord}
import org.scalatest.WordSpecLike
import scala.io.Source._
import scala.collection.immutable.{Iterable, Seq}
import org.gs._
import org.gs.avro._
import org.gs.avro.stream.{AvroDeserializer, AvroSerializer}
import org.gs.examples.account.GetAccountBalances
import org.gs.examples.account.avro._
import org.gs.examples.account.kafka.AccountProducer
import org.gs.examples.account.kafka.fixtures.{AccountConsumerFixture, AccountProducerFixture}

/** 2 Akka streams, The first creates a simple Source with an iterable of case classes, then a Flow
  * serializes them with their avro schema to byte arrays, then a KafkaSink writes them to Kafka.
  * The second stream has a KafkaSource that reads the serialized case classes from Kafka, then a
  * Flow maps ConsumerRecords to a queue of ConsumerRecord, then a Flow maps a ConsumerRecord to its
  * value, then a Flow maps the serialized value back to a case class, then a TestSink compares
  * deserialized case classes to the originals. The TestSink pulls elements from the stream.
  *
  * @author Gary Struthers
  *
  */
class KafkaStreamSpec extends WordSpecLike with AccountProducerFixture with AccountConsumerFixture {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

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
      val sinkGraph = KafkaSink[Array[Byte],String, Array[Byte]](ap)
      val sink = Sink.fromGraph(sinkGraph)
      val consumerRecordQueue = new ConsumerRecordQueue[String, Array[Byte]]()
      val deserializer = new AvroDeserializer("getAccountBalances.avsc", genericRecordToGetAccountBalances)
      Source[GetAccountBalances](iter)
        .via(serializer)
        .runWith(sink)

      val sourceGraph = new KafkaSource[String, Array[Byte]](accountConsumerFacade)
      val source = Source.fromGraph(sourceGraph)
      val sub = source
          .via(consumerRecordsFlow[String, Array[Byte]])
          .via(consumerRecordQueue)
          .via(consumerRecordValueFlow)
          .via(deserializer)
          .runWith(TestSink.probe[GetAccountBalances])
      val iterTest = Iterable(getBals.toSeq:_*)
      iterTest.foreach(x => assert(sub.requestNext() === x))
      sub.expectComplete()
    }
  }
}
