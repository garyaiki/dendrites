package org.gs.examples.account.kafka

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata }
import org.gs.examples.account.GetAccountBalances
import org.gs.kafka.{ProducerClient, WrappedProducer}

/** Create KafkaProducer for GetAccountBalances
  *
  * Value is just the Long id of GetAccountBalances
  * lastSend can be used by consumer seek to latest record
  * @see https://kafka.apache.org/090/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html
  */
object AccountProducer extends WrappedProducer[Array[Byte],String, Array[Byte]] {

  def apply(): ProducerClient[Key, Value] = {
    new ProducerClient[Key, Value]("dendrites", "blocking-dispatcher", "kafkaProducer.properties")
  }

  val client = apply()
  val config = ConfigFactory.load()
  val topic = config.getString("dendrites.kafka.account.topic")
  val key = config.getString("dendrites.kafka.account.key")
  def send(item: InType): Either[String, RecordMetadata] = {
    val producerRecord = new ProducerRecord[Key, Value](topic, key, item)
    client.send(producerRecord)(client.ec)
  }
}