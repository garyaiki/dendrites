package org.gs.kafka

import java.util.{List => JList}
import org.apache.kafka.clients.consumer.Consumer

/** Abstract KafkaConsumer configuration and factory
  * 
  * @tparam <K> Kafka ConsumerRecord key
  * @tparam <V> Kafka ConsumerRecord value
  *
  * @author Gary Struthers
  */
trait ConsumerConfig[K, V] {
  type Key = K
  type Value = V 

  val topics: JList[String]
  val timeout: Long // how many milliseconds to wait for poll to return data

  def createAndSubscribe(): Consumer[Key, Value]  
}
