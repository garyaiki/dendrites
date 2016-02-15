package org.gs.kafka

import java.util.{ List => JList, Properties }
import org.apache.kafka.clients.consumer.{ Consumer, ConsumerRecords, KafkaConsumer }

/** Abstract wraper of KafkaConsumer, implementation inits KafkaConsumer properties, creates a
  * KafkaConsumer, and subscribes it to 1 or more topics in a list
  * 
  * @author Gary Struthers
  * @tparam <K> Kafka ConsumerRecord key
  * @tparam <V> Kafka ConsumerRecord value
  */
trait ConsumerFacade[K, V] {
  type Key = K
  type Value = V 

  val props: Properties
  val topics: JList[String]
  val timeout: Long // how many milliseconds to wait for poll to return data

  /** Create KafkaConsumer with its properties, subscribe to topic(s), consumers die if 
    * session.timeout and have to be recreated
   	*
   	* @return initialized KafkaConsumer
    */
  def apply(): Consumer[Key, Value] = {
    val kc = new KafkaConsumer[Key, Value](props)
    kc.subscribe(topics)
    kc
  }

  /** Poll Kafka log and wait timeout milliseconds for data
   	*
   	* @param KafkaConsumer
   	* @return ConsumerRecords containing message data and Kafka metadata
    */  
  def poll(consumer: Consumer[Key, Value]): ConsumerRecords[Key, Value] = {
    consumer.poll(timeout)
  }
}