/**
  */
package org.gs.kafka

import akka.actor.ActorSystem
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import scala.io.Source._
import scala.collection.immutable.{Iterable, Seq}
import scala.concurrent.duration.MILLISECONDS
import org.gs._
import org.gs.avro._
import org.gs.examples.account.GetAccountBalances

/** 
  *
  * @author Gary Struthers
  *
  */
class AvroKafkaProducerSpec extends WordSpecLike with BeforeAndAfterAll {
  implicit val system = ActorSystem("dendrites")
  implicit val logger = Logging(system, getClass)
  val config = ConfigFactory.load()
  val timeout = config.getLong("dendrites.kafka.account.close-timeout")
  val topic = config.getString("dendrites.kafka.account.topic")
  val key = config.getString("dendrites.kafka.account.key")
  val schema = loadSchema("getAccountBalances.avsc")
  var producer: KafkaProducer[String, Array[Byte]] = null
  override def beforeAll() {
    producer = createProducer[String, Array[Byte]]("kafkaProducer.properties")
  }


  "A KafkaProducer" should {
    "send a message" in {
      val gab = GetAccountBalances(1L)
      val bytes = ccToByteArray(schema, gab)
      val record = new ProducerRecord[String, Array[Byte]](topic, key, bytes)

      val rm: RecordMetadata = producer.send(record).get()
      assert(rm != null)
    }
  }

  override def afterAll() {
    producer.flush()
    producer.close(timeout, MILLISECONDS)    
  }
}
