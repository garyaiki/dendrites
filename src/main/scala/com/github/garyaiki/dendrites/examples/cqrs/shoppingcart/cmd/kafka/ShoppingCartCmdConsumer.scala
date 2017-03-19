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
package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.kafka

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.Consumer
import scala.collection.JavaConverters._
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import com.github.garyaiki.dendrites.concurrent.calculateDelay
import com.github.garyaiki.dendrites.kafka.ConsumerConfig
import com.github.garyaiki.dendrites.kafka.createConsumer

/** Configure and create KafkaConsumer, calculateDelay constant args, subscribe to shoppingcartcmd topic
  *
  * @author Gary Struthers
  */
object ShoppingCartCmdConsumer extends ConsumerConfig[String, Array[Byte]] {
  val config = ConfigFactory.load
  val topic = config getString("dendrites.kafka.shoppingcartcmd.topic")
  val topics = List(topic).asJava
  val timeout = config getLong("dendrites.kafka.poll-timeout")
  val min = config getInt("dendrites.timer.min-backoff")
  val minDuration = FiniteDuration(min, MILLISECONDS)
  val max = config getInt("dendrites.timer.max-backoff")
  val maxDuration = FiniteDuration(max, MILLISECONDS)
  val randomFactor = config getDouble("dendrites.timer.randomFactor")
  val curriedDelay = calculateDelay(minDuration, maxDuration, randomFactor) _

  /** Create consumer with configuration properties, subscribe to account topic
    * @return consumer
    */
  def createAndSubscribe(): Consumer[Key, Value] = {
    val c = createConsumer[Key, Value]("kafkaConsumer.properties")
    c subscribe(topics)
    c
  }
}
