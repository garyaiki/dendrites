package org.gs.kafka

import org.apache.kafka.clients.producer.Producer

/** Abstraction for KafkaProducer
 *
 * 
 * @author Gary Struthers
 * @tparam <K> Kafka ProducerRecord key
 * @tparam <V> Kafka ProducerRecord value
 */
trait WrappedProducer[K, V] {
  type Key = K
  type Value = V

  val producer: Producer[K, V]
  val topic: String
  val key: Key
}
