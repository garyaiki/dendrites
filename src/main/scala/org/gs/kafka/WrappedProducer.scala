package org.gs.kafka

import org.apache.kafka.clients.producer.RecordMetadata

/** Abstraction to use KafkaProducer in a Sink
 *
 * 
 * @author Gary Struthers
 * @tparam <A> Type received from stream
 * @tparam <K> Kafka ProducerRecord key
 * @tparam <V> Kafka ProducerRecord value, may be the same or different than A
 */
trait WrappedProducer[A, K, V] {
  type InType = A
  type Key = K
  type Value = V

  def send(item: InType): Either[String, RecordMetadata]
}
