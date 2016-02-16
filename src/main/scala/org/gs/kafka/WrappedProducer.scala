package org.gs.kafka

import org.apache.kafka.clients.producer.RecordMetadata

/** Abstraction to use KafkaProducer in a Sink
 *
 * 
 * @author Gary Struthers
 * @tparam <K> Kafka ProducerRecord key
 * @tparam <V> Kafka ProducerRecord value
 */
trait WrappedProducer[K, V] {
  type Key = K
  type Value = V

  def send(item: Value): Either[String, RecordMetadata]
}
