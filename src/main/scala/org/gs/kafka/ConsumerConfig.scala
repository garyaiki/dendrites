package org.gs.kafka

import java.util.{ List => JList, Properties }
import org.apache.kafka.clients.consumer.{ Consumer, ConsumerRecords, KafkaConsumer }

/** Abstract configuration of KafkaConsumer
  * 
  * @author Gary Struthers
  * @tparam <K> Kafka ConsumerRecord key
  * @tparam <V> Kafka ConsumerRecord value
  */
trait ConsumerConfig[K, V] {
  type Key = K
  type Value = V 

  val props: Properties
  val topics: JList[String]
  val timeout: Long // how many milliseconds to wait for poll to return data

  def createConsumer(): Consumer[Key, Value]  
}