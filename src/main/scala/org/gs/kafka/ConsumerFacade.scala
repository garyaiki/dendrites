package org.gs.kafka

import java.util.{List => JList, Properties}
import org.apache.kafka.clients.consumer.{ Consumer, ConsumerRecords, KafkaConsumer}
import akka.stream.Outlet

/** Abstraction to use KafkaConsumer in a Source
 * @author Gary Struthers
 *
 * @tparam <K> Kafka ConsumerRecord key
 * @tparam <V> Kafka ConsumerRecord value
 */
trait ConsumerFacade[K, V] {
  type Key = K
  type Value = V
  type OutType = ConsumerRecords[Key, Value]

  val props: Properties
  val topics: JList[String]
  val timeout: Long
  
  def apply(): Consumer[Key, Value] = {
    val kc = new KafkaConsumer[Key, Value](props)
    kc.subscribe(topics)
    kc
  }
  
  def poll(consumer: Consumer[Key, Value]): OutType = {
    consumer.poll(timeout)
  }
}