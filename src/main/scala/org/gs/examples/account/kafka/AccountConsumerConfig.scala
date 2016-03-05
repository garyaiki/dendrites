package org.gs.examples.account.kafka

import com.typesafe.config.ConfigFactory
import java.util.{ List => JList, Properties}
import org.apache.kafka.clients.consumer.{ Consumer, ConsumerRecords, KafkaConsumer}
import akka.stream.Outlet
import scala.collection.JavaConverters._
import org.gs._
import org.gs.kafka.ConsumerConfig

/** KafkaConsumer properties specific to account topic
  *
  * @author Gary Struthers
 */
object AccountConsumerConfig extends ConsumerConfig[String, Array[Byte]] {
  val props = loadProperties("kafkaConsumer.properties")
  val config = ConfigFactory.load()
  val topic = config.getString("dendrites.kafka.account.topic")
  val topics = List(topic).asJava
  val timeout = config.getLong("dendrites.kafka.account.poll-timeout")

  def createConsumer(): Consumer[Key, Value] = {
    val c = new KafkaConsumer[Key, Value](props)
    c.subscribe(topics)
    c
  }
}
