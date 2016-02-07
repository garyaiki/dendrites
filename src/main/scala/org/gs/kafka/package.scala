package org.gs

import java.util.Properties
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.gs._

package object kafka {

  /** Create KafkaConsumer consumer is not thread safe
    *
    * Call from an object singleton for distinct properties and KV types
    * @see https://kafka.apache.org/090/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html 
    * @tparam K key
    * @tparam V value
    * @param filename Kafka consumer properties
    * @return
    */
  def createConsumer[K, V](filename: String): KafkaConsumer[K, V] = {
    val props = loadProperties(filename)
    new KafkaConsumer[K, V](props)
  }

  /** Create KafkaProducer, producer is thread safe
    *
    * Call from an object singleton for distinct properties and KV types
    * @see https://kafka.apache.org/090/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html
    * @tparam K key
    * @tparam V value
    * @param filename Kafka producer properties
    * @return
    */
  def createProducer[K, V](filename: String): KafkaProducer[K, V] = {
    val props = loadProperties(filename)
    new KafkaProducer[K, V](props)
  }
}