package org.gs

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer

/** Provides Classes to add fields to KafkaConsumer and KafkaProducer. Also Factories for
  * KafkaConsumer, KafkaProducer from their Properties files
  *
  * Create KafkaConsumer with properties
  * {{{
  * val consumer = createConsumer[Key, Value]("kafkaConsumer.properties")
  * }}}
  * Create KafkaProducer with properties
  * {{{
  * val producer = createProducer[Key, Value]("kafkaProducer.properties")
  * }}}
  * @see [[http://typesafehub.github.io/config/latest/api/ Config API]]
  * @author Gary Struthers
  */
package object kafka {

  /** Create KafkaConsumer Java client configured with its properties, consumer is NOT thread safe
    *
    * @tparam K Kafka ConsumerRecord key
    * @tparam V Kafka ConsumerRecord value
    * @param filename Kafka consumer properties
    * @return consumer
    */
  def createConsumer[K, V](filename: String): KafkaConsumer[K, V] = {
    val props = loadProperties(filename)
    new KafkaConsumer[K, V](props)
  }

  /** Create KafkaProducer Java client configured with its properties, KafkaProducer IS thread safe
    *
    * @tparam K Kafka ProducerRecord key
    * @tparam V Kafka ProducerRecord value
    * @param filename Kafka producer properties
    * @return producer
    */
  def createProducer[K, V](filename: String): KafkaProducer[K, V] = {
    val props = loadProperties(filename)
    new KafkaProducer[K, V](props)
  }
}
