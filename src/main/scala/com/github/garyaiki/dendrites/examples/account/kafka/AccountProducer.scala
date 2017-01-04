/** Copyright 2016 Gary Struthers

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
package com.github.garyaiki.dendrites.examples.account.kafka

import com.typesafe.config.ConfigFactory
import java.util.UUID
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import com.github.garyaiki.dendrites.concurrent.calculateDelay
import com.github.garyaiki.dendrites.kafka.ProducerConfig
import com.github.garyaiki.dendrites.kafka.createProducer

/** Create KafkaProducer configured for GetAccountBalances
  *
  */
object AccountProducer extends ProducerConfig[String, Array[Byte]] {

  override def generateKey = UUID.randomUUID.toString

  val config = ConfigFactory.load
  val topic = config.getString("dendrites.kafka.account.topic")
  val producer = createProducer[Key, Value]("kafkaProducer.properties")
  val min = config getInt("dendrites.kafka.account.min-backoff")
  val minDuration = FiniteDuration(min, MILLISECONDS)
  val max = config getInt("dendrites.kafka.account.max-backoff")
  val maxDuration = FiniteDuration(max, MILLISECONDS)
  val randomFactor = config getDouble("dendrites.kafka.account.randomFactor")
  val curriedDelay = calculateDelay(minDuration, maxDuration, randomFactor) _
}
