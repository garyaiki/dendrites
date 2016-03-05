/**
  */
package org.gs.kafka

import akka.actor.ActorSystem
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import java.util.ArrayList
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import scala.io.Source._
import scala.collection.immutable.{Iterable, Seq}
import scala.concurrent.duration.MILLISECONDS
import org.gs._


/** 
  *
  * @author Gary Struthers
  *
  */
class KafkaProducerConsumerSpec extends WordSpecLike with BeforeAndAfterAll {
  implicit val system = ActorSystem("dendrites")
  implicit val logger = Logging(system, getClass)
  val config = ConfigFactory.load()
  val timeout = config.getLong("dendrites.kafka.account.close-timeout")
  val topic = "test-topic"
  val topics = new ArrayList[String]()
  topics.add(topic)
  val key = "test-key"
  val value = "test-value"
  var producer: KafkaProducer[String, String] = null
  var consumer: KafkaConsumer[String, String] = null
  override def beforeAll() {
    producer = createProducer[String, String]("testKafkaProducer.properties")
    consumer = createConsumer[String, String]("testKafkaConsumer.properties")
    consumer.subscribe(topics)
  }


  "A KafkaProducerConsumer" should {
    "send a message" in {
      val record = new ProducerRecord[String, String](topic, key, value)

      val rm: RecordMetadata = producer.send(record).get()
      assert(rm != null)
      assert(rm.topic === topic)
  
    }
    "read the message" in {
      val crs = consumer.poll(timeout)
      assert(crs.count === 1)
      val it = crs.iterator()
      while(it.hasNext()) {
        val cr = it.next()
        assert(cr.key() === key)
        assert(cr.value() === value)
      }
    }
  }

  override def afterAll() {
    consumer.commitSync()
    consumer.close()
    producer.flush()
    producer.close(timeout, MILLISECONDS)    
  }
}
