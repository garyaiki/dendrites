package org.gs

import java.util.Properties
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.gs._

package object kafka {

  /** Create KafkaConsumer Java client configured with its properties, consumer is not thread safe
    *
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

  /** Create KafkaProducer Java client configured with its properties, KafkaProducer is thread safe
    *
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