package org.gs.examples.account.kafka

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.{Consumer, KafkaConsumer}
import scala.collection.JavaConverters._
import org.gs.loadProperties
import org.gs.kafka.ConsumerConfig

/** Configure and create KafkaConsumer, subscribe to account topic
  *
  * @author Gary Struthers
  */
object AccountConsumer extends ConsumerConfig[String, Array[Byte]] {
  val props = loadProperties("kafkaConsumer.properties")
  val config = ConfigFactory.load()
  val topic = config getString("dendrites.kafka.account.topic")
  val topics = List(topic).asJava
  val timeout = config getLong("dendrites.kafka.account.poll-timeout")

  /** Create consumer with configuration properties, subscribe to account topic
    * @return consumer
    */
  def createConsumer(): Consumer[Key, Value] = {
    val c = new KafkaConsumer[Key, Value](props)
    c subscribe(topics)
    c
  }
}
