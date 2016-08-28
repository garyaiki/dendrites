package org.gs.examples.account.kafka

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.Consumer
import scala.collection.JavaConverters._
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import org.gs.concurrent.calculateDelay
import org.gs.kafka.ConsumerConfig
import org.gs.kafka.createConsumer

/** Configure and create KafkaConsumer, curry calculateDelay constants, subscribe to account topic
  *
  * @author Gary Struthers
  */
object AccountConsumer extends ConsumerConfig[String, Array[Byte]] {
  val config = ConfigFactory.load()
  val topic = config getString("dendrites.kafka.account.topic")
  val topics = List(topic).asJava
  val timeout = config getLong("dendrites.kafka.account.poll-timeout")
  val min = config getInt("dendrites.kafka.account.min-backoff")
  val minDuration = FiniteDuration(min, MILLISECONDS)
  val max = config getInt("dendrites.kafka.account.max-backoff")
  val maxDuration = FiniteDuration(max, MILLISECONDS)
  val randomFactor = config getDouble("dendrites.kafka.account.randomFactor")
  val curriedDelay = calculateDelay(minDuration, maxDuration, 0.2) _

  /** Create consumer with configuration properties, subscribe to account topic
    * @return consumer
    */
  def createAndSubscribe(): Consumer[Key, Value] = {
    val c = createConsumer[Key, Value]("kafkaConsumer.properties")
    c subscribe(topics)
    c
  }
}
