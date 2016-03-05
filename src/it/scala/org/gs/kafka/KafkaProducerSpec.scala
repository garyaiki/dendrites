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


/** 
  *
  * @author Gary Struthers
  *
  */
class KafkaProducerSpec extends WordSpecLike with BeforeAndAfterAll {
  implicit val system = ActorSystem("dendrites")
  implicit val logger = Logging(system, getClass)
  val config = ConfigFactory.load()
  val timeout = config.getLong("dendrites.kafka.account.close-timeout")
  var producer: KafkaProducer[String, String] = null
  override def beforeAll() {
    producer = createProducer[String, String]("debugKafkaProducer.properties")
  }


  "A KafkaProducer" should {
    "send a message" in {
      val record = new ProducerRecord[String, String]("debug-topic", "debug-key", "debug-item")

      val rm: RecordMetadata = producer.send(record).get()
      assert(rm != null)
    }
  }

  override def afterAll() {
    producer.flush()
    producer.close(timeout, MILLISECONDS)    
  }
}
