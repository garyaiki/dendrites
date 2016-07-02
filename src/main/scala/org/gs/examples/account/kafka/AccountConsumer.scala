package org.gs.examples.account.kafka

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.Consumer
import scala.collection.JavaConverters._
import org.gs.kafka.ConsumerConfig
import org.gs.kafka.createConsumer

/** Configure and create KafkaConsumer, subscribe to account topic
  *
  * @author Gary Struthers
  */
object AccountConsumer extends ConsumerConfig[String, Array[Byte]] {
  val config = ConfigFactory.load()
  val topic = config getString("dendrites.kafka.account.topic")
  val topics = List(topic).asJava
  val timeout = config getLong("dendrites.kafka.account.poll-timeout")

  /** Create consumer with configuration properties, subscribe to account topic
    * @return consumer
    */
  def createAndSubscribe(): Consumer[Key, Value] = {
    val c = createConsumer[Key, Value]("kafkaConsumer.properties")
    c subscribe(topics)
    c
  }
}
