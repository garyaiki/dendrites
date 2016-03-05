/**
  */
package org.gs.kafka

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import com.typesafe.config.ConfigFactory
import org.apache.avro.generic.{GenericDatumReader, GenericRecord}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import scala.io.Source._
import scala.collection.immutable.{Iterable, Seq}
import scala.concurrent.duration.MILLISECONDS
import org.gs._
import org.gs.avro._
import org.gs.avro.stream.{AvroDeserializer, AvroSerializer}
import org.gs.examples.account.GetAccountBalances
import org.gs.examples.account.avro._
import org.gs.examples.account.kafka.{AccountConsumerConfig, AccountProducer}
import org.gs.examples.account.kafka.fixtures.AccountProducerFixture

/** 2 Akka streams, The first creates a Source with an iterable of case classes, a Flow
  * serializes them with their avro schema to byte arrays, then a KafkaSink writes them to Kafka.
  * The second stream has a KafkaSource that reads from Kafka, a Flow maps ConsumerRecords to a 
  * queue of ConsumerRecord, a Flow extracts the value of a ConsumerRecord, a Flow deserializes the
  * value back to a case class, then a TestSink compares received case classes to the originals.
  * The TestSink pulls elements from the stream, when consumerRecordQueue is pulled it dequeues 1
  * ConsumerRecord, when the queue becomes empty a pull request is passed back to the KafkaSource
  * which commits the messages read in the last poll and then polls again.
  *
  * @author Gary Struthers
  *
  */
class KafkaConsumerSpec extends WordSpecLike {
  implicit val system = ActorSystem("dendrites")
  implicit val logger = Logging(system, getClass)
  val config = ConfigFactory.load()
  val timeout = config.getLong("dendrites.kafka.account.close-timeout")
  val accountConsumerConfig = AccountConsumerConfig


  "An KafkaConsumer" should {
    "read from Kafka" in {

      val kafkaConsumer = accountConsumerConfig.createConsumer()
      val subscribedTopics = kafkaConsumer.subscription()
      val itT = subscribedTopics.iterator()
      var subscribedTopic: String = ""
      while(itT.hasNext()) {
        subscribedTopic = itT.next()
        logger.debug(s"subscribed topic:$subscribedTopic")
      }
      logger.debug(s"before poll")
      val crs = kafkaConsumer.poll(timeout)
      logger.debug(s"records polled:${crs.count()}")
      val it = crs.iterator()
      while(it.hasNext()) {
        val cr = it.next()
        logger.debug(s"ConsumerRecord:${cr.toString()}")
      }

    }
  }

}
