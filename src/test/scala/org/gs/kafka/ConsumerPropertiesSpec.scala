package org.gs.kafka

import java.util.Properties
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.gs.loadProperties
import org.scalatest.WordSpecLike
import org.scalatest._
import org.scalatest.Matchers._

class ConsumerPropertiesSpec extends WordSpecLike {
  val prop: Properties = loadProperties("kafkaConsumer.properties")
  "An Properties" should {
    "have a bootstrap.servers" in {
      prop.getProperty("bootstrap.servers") should equal("localhost:9092")
    }
    "have a group.id" in {
      prop.getProperty("group.id") should equal("\"\"")
    }
    "have a enable.auto.commit set false" in {
      prop.getProperty("enable.auto.commit") should equal("false")
    }
    "have a auto.commit.interval.ms" in {
      prop.getProperty("auto.commit.interval.ms") should equal("1000")
    }
    "have a session.timeout.ms" in {
      prop.getProperty("session.timeout.ms") should equal("30000")
    }
    "have a key.deserializer" in {
      prop.getProperty("key.deserializer") should equal(
          "org.apache.kafka.common.serialization.StringDeserializer")
    }
    "have a value.deserializer" in {
      prop.getProperty("value.deserializer") should equal(
          "org.apache.kafka.common.serialization.ByteArrayDeserializer")
    }
    "create a KafkaConsumer" in {
      val consumer = new KafkaConsumer(prop)
      consumer.close()
    }
  }
}
