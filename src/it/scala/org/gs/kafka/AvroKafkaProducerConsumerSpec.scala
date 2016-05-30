/**
  */
package org.gs.kafka

import akka.actor.ActorSystem
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import java.util.ArrayList
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import scala.collection.immutable.{Iterable, Seq}
import scala.concurrent.duration.MILLISECONDS
import scala.io.Source._
import org.gs.avro.{byteArrayToGenericRecord, ccToByteArray, loadSchema}
import org.gs.examples.account.GetAccountBalances
import org.gs.examples.account.avro.genericRecordToGetAccountBalances

/** 
  *
  * @author Gary Struthers
  *
  */
class AvroKafkaProducerConsumerSpec extends WordSpecLike with BeforeAndAfterAll {
  implicit val system = ActorSystem("dendrites")
  implicit val logger = Logging(system, getClass)
  val config = ConfigFactory.load()
  val timeout = config.getLong("dendrites.kafka.account.close-timeout")
  val topic = "avroTest-topic"
  val topics = new ArrayList[String]()
  topics.add(topic)
  val key = config.getString("dendrites.kafka.account.key")
  val schema = loadSchema("getAccountBalances.avsc")
  val gab = GetAccountBalances(1L)
  var producer: KafkaProducer[String, Array[Byte]] = null
  var consumer: KafkaConsumer[String, Array[Byte]] = null
  override def beforeAll() {
    producer = createProducer[String, Array[Byte]]("testAvroKafkaProducer.properties")
    consumer = createConsumer[String, Array[Byte]]("testAvroKafkaConsumer.properties")
    consumer.subscribe(topics)
  }


  "A AvroKafkaProducerConsumer" should {
    "serialize a case class and send a message" in {
      val bytes = ccToByteArray(schema, gab)
      val record = new ProducerRecord[String, Array[Byte]](topic, key, bytes)
      val rm: RecordMetadata = producer.send(record).get()
      assert(rm != null)
      assert(rm.topic === topic)
  
    }
    "read the message" in {
      val crs = consumer.poll(timeout)
      assert(crs.count === 1)
      val it = crs.iterator()
      var consumerRecord: ConsumerRecord[String, Array[Byte]] = null
      while(it.hasNext()) {
        consumerRecord = it.next()
      }
      val value = consumerRecord.value()
      val genericRecord: GenericRecord = byteArrayToGenericRecord(schema, value)
      val accountBalances = genericRecordToGetAccountBalances(genericRecord)
      assert(accountBalances === gab)
    }
  }

  override def afterAll() {
    consumer.commitSync()
    consumer.close()
    producer.flush()
    producer.close(timeout, MILLISECONDS)    
  }
}
