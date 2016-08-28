package org.gs.kafka

import java.util.{List => JList}
import org.apache.kafka.clients.consumer.Consumer
import scala.concurrent.duration.FiniteDuration

/** Abstract KafkaConsumer configuration and factory
  *
  * @tparam <K> Kafka ConsumerRecord key
  * @tparam <V> Kafka ConsumerRecord value
  *
  * @author Gary Struthers
  */
trait ConsumerConfig[K, V] {
  type Key = K
  type Value = V

  val topics: JList[String]
  val timeout: Long // how many milliseconds to wait for poll to return data
  val minDuration: FiniteDuration // min poll, commit backoff
  val maxDuration: FiniteDuration // max poll, commit backoff
  val randomFactor: Double // random delay factor between 0.0, 1.0
  val curriedDelay: Int => FiniteDuration // curried calculateDelay

  def createAndSubscribe(): Consumer[Key, Value]
}
