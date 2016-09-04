/** Copyright 2016 Gary Struthers

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.gs.kafka

import java.util.{List => JList}
import org.apache.kafka.clients.consumer.Consumer
import scala.concurrent.duration.FiniteDuration

/** Abstract KafkaConsumer configuration and factory
  *
  * @tparam K ConsumerRecord key
  * @tparam V ConsumerRecord value
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
