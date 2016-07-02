package org.gs.kafka

import org.apache.kafka.clients.producer.Producer

/** Abstract KafkaProducer configuration
 *
 * 
 * @tparam <K> Kafka ProducerRecord key
 * @tparam <V> Kafka ProducerRecord value
 *
 * @author Gary Struthers
 * 
 */
trait ProducerConfig[K, V] {
  type Key = K
  type Value = V

  val producer: Producer[K, V]
  val topic: String
  val key: Key
}
