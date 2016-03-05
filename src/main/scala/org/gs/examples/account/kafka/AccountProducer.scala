package org.gs.examples.account.kafka

import com.typesafe.config.ConfigFactory
import org.gs.kafka.WrappedProducer
import org.gs.kafka._

/** Create KafkaProducer for GetAccountBalances
  *
  * Value is the serialized id of GetAccountBalances
  * @see https://kafka.apache.org/090/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html
  */
object AccountProducer extends WrappedProducer[String, Array[Byte]] {

  val config = ConfigFactory.load()
  val topic = config.getString("dendrites.kafka.account.topic")
  val key = config.getString("dendrites.kafka.account.key")
  val producer = createProducer[Key, Value]("kafkaProducer.properties")

}