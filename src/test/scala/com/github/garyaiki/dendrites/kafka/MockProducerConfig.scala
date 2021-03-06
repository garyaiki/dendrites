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

import java.util.UUID
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import com.github.garyaiki.dendrites.concurrent.calculateDelay

object MockProducerConfig extends ProducerConfig[String, Array[Byte]] {

  override def generateKey(): String = UUID.randomUUID.toString

  val topic = "akkaKafka"
  val autoComplete = true // When false must call completeNext or errorNext for each record
  val minDuration = FiniteDuration(100, MILLISECONDS)
  val maxDuration = FiniteDuration(1000, MILLISECONDS)
  val randomFactor = 0.2
  val curriedDelay = calculateDelay(minDuration, maxDuration, randomFactor) _
  val producer = new MockProducer(autoComplete, new StringSerializer, new ByteArraySerializer)
}
