package org.gs

import java.util.Properties
import org.scalatest.WordSpecLike
import org.scalatest._
import org.scalatest.Matchers._

class PropertiesSpec extends WordSpecLike {
  val prop: Properties = loadProperties(new StringBuilder("kafkaProducer.properties"))
  "An Properties" should {
    "have a bootstrap.servers" in {
      prop.getProperty("bootstrap.servers") should equal("localhost:9092")
    }
    "have an acks" in {
      prop.getProperty("acks") should equal("all")
    }
    "have a retries" in {
      prop.getProperty("retries") should equal("0")
    }
    "have a batch.size" in {
      prop.getProperty("batch.size") should equal("16384")
    }
    "have a linger.ms" in {
      prop.getProperty("linger.ms") should equal("1")
    }
    "have a key.serializer" in {
      prop.getProperty("key.serializer") should equal(
          "org.apache.kafka.common.serialization.StringSerializer")
    }
    "have a value.serializer" in {
      prop.getProperty("value.serializer") should equal(
          "org.apache.kafka.common.serialization.LongSerializer")
    }
  }
}
