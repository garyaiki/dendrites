package org.gs.examples.account.kafka

import com.typesafe.config.ConfigFactory
import java.util.{ List => JList, Properties}
import org.apache.kafka.clients.consumer.{ ConsumerRecords, KafkaConsumer}
import akka.stream.Outlet
import scala.collection.JavaConverters._
import org.gs._
import org.gs.kafka.ConsumerFacade

object AccountConsumer extends ConsumerFacade[String, Long] {
  val props = loadProperties(new StringBuilder("kafkaConsumer.properties"))
  val config = ConfigFactory.load()
  val topic = config.getString("dendrites.kafka.account.topic")
  val topics = List(topic).asJava
  val timeout = config.getLong("dendrites.kafka.account.poll-timeout")
}
