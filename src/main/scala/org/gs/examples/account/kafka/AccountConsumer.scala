package org.gs.examples.account.kafka

import com.typesafe.config.ConfigFactory
import java.util.{ List => JList, Properties, Queue }
import java.util.concurrent.ConcurrentLinkedQueue
//import org.apache.kafka.clients.consumer.{ ConsumerRecord, KafkaConsumer}
import scala.collection.JavaConverters._
import org.gs._
import org.gs.examples.account.GetAccountBalances
import org.gs.kafka.{ ConsumerClient, WrappedConsumer }


object AccountConsumer extends WrappedConsumer[GetAccountBalances, String, Long] {
  val queue = new ConcurrentLinkedQueue[Long]()
  val props = loadProperties(new StringBuilder("kafkaConsumer.properties"))
  val config = ConfigFactory.load()
  val topic = config.getString("dendrites.kafka.account.topic")
  val topicList = List(topic).asJava
  val timeout = config.getLong("dendrites.kafka.account.poll-timeout")

  def apply(queue: Queue[Long]) = {
    new ConsumerClient[String, Long](queue, props, topicList, timeout)
  }

  val consumerClient = apply(queue)

  def poll(): Unit = {
    consumerClient.poll()
  }
  
  def next(): Option[GetAccountBalances] = {
    consumerClient.next() match {
      case Some(x) => Some(GetAccountBalances(x))
      case _ => None
    }
  }
}