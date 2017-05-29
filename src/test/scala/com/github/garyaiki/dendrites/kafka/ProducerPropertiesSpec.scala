/**
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
package com.github.garyaiki.dendrites.kafka

import java.util.Properties
import org.apache.kafka.clients.producer.KafkaProducer
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import com.github.garyaiki.dendrites.loadProperties

class ProducerPropertiesSpec extends WordSpecLike {
  val prop: Properties = loadProperties("kafkaProducer.properties")
  "An Properties" should {
    "have a bootstrap.servers" in { prop.getProperty("bootstrap.servers") should equal("localhost:9092") }

    "have an acks" in { prop.getProperty("acks") should equal("all") }

    "have a retries" in { prop.getProperty("retries") should equal("3") }

    "have a batch.size" in { prop.getProperty("batch.size") should equal("16384") }

    "have a linger.ms" in { prop.getProperty("linger.ms") should equal("1") }

    "have a key.serializer" in {
      prop.getProperty("key.serializer") should equal("org.apache.kafka.common.serialization.StringSerializer")
    }

    "have a value.serializer" in {
      prop.getProperty("value.serializer") should equal("org.apache.kafka.common.serialization.ByteArraySerializer")
    }

    "create a KafkaProducer" in {
      val producer = new KafkaProducer(prop)
      producer should not be (null)
      producer.close()
    }
  }
}
