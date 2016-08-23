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

import org.apache.kafka.clients.producer.Producer

/** Abstract KafkaProducer configuration
 *
 *
 * @tparam <K> Kafka ProducerRecord key
 * @tparam <V> Kafka ProducerRecord value
 *
 * @author Gary Struthers
 *
 */
trait ProducerConfig[K, V] {
  type Key = K
  type Value = V

  val producer: Producer[K, V]
  val topic: String
  val key: Key
}