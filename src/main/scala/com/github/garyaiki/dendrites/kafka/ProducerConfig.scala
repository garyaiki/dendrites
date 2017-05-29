/**
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
package com.github.garyaiki.dendrites.kafka

import org.apache.kafka.clients.producer.Producer
import scala.concurrent.duration.FiniteDuration

/** Abstract KafkaProducer configuration
 *
 *
 * @tparam K ProducerRecord key
 * @tparam V ProducerRecord value
 *
 * @author Gary Struthers
 *
 */
trait ProducerConfig[K, V] {
  type Key = K
  type Value = V

  def generateKey(): K

  val minDuration: FiniteDuration // min poll, commit backoff
  val maxDuration: FiniteDuration // max poll, commit backoff
  val randomFactor: Double // random delay factor between 0.0, 1.0
  val curriedDelay: Int => FiniteDuration // curried calculateDelay

  val producer: Producer[K, V]
  val topic: String
}
