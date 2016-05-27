package org.gs.examples.account.kafka

import com.typesafe.config.ConfigFactory
import org.gs.kafka.WrappedProducer
import org.gs.kafka.createProducer

/** Create KafkaProducer for GetAccountBalances
  *
  * Value is the serialized id of GetAccountBalances
  */
object AccountProducer extends WrappedProducer[String, Array[Byte]] {

  val config = ConfigFactory.load()
  val topic = config.getString("dendrites.kafka.account.topic")
  val key = config.getString("dendrites.kafka.account.key")
  val producer = createProducer[Key, Value]("kafkaProducer.properties")
}
