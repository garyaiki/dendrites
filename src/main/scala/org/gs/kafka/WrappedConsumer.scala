package org.gs.kafka

import java.util.Queue
import org.apache.kafka.clients.consumer.{ ConsumerRecord, KafkaConsumer}

/** Abstraction to use KafkaConsumer in a Source
 * @author garystruthers
 *
 * @tparam <A> Type sent to stream
 * @tparam <K> Kafka ConsumerRecord key
 * @tparam <V> Kafka ConsumerRecord type, may be the same or different than A
 */
trait WrappedConsumer[A, K, V] {
  type OutType = A
  type Key = K
  type Value = V

  def poll(): Unit
  
  def next(): Option[OutType]
}