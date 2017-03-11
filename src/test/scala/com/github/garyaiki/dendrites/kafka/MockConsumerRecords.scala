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

import java.util.{ArrayList, HashMap, List => JList}
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords}
import org.apache.kafka.common.TopicPartition

/** Test topic, partitions, and ConsumerRecords for MockConsumer
  *
  * @author Gary Struthers
  *
  */
trait MockConsumerRecords {
  val topic = "akkaKafka"
  val key = topic + "Key"

  val cRecordList0: JList[ConsumerRecord[String, String]] = new ArrayList[ConsumerRecord[String, String]]()
  cRecordList0.add(new ConsumerRecord[String, String](topic, 0, 0L, key, "0"))
  cRecordList0.add(new ConsumerRecord[String, String](topic, 0, 10L, key, "10"))
  cRecordList0.add(new ConsumerRecord[String, String](topic, 0, 20L, key, "20"))

  val topicPartition0 = new TopicPartition(topic, 0)
  val map0 = new HashMap[TopicPartition, JList[ConsumerRecord[String, String]]]()
  map0.put(topicPartition0, cRecordList0)
  val cRecords0 = new ConsumerRecords(map0)

  val cRecordList1 = new ArrayList[ConsumerRecord[String, String]]()
  cRecordList1.add(new ConsumerRecord[String, String](topic, 1, 0L, key, "5"))
  cRecordList1.add(new ConsumerRecord[String, String](topic, 1, 10L, key, "15"))
  cRecordList1.add(new ConsumerRecord[String, String](topic, 1, 20L, key, "25"))
  cRecordList1.add(new ConsumerRecord[String, String](topic, 1, 30L, key, "35"))
  val topicPartition1 = new TopicPartition(topic, 1)
  val map1 = new HashMap[TopicPartition, JList[ConsumerRecord[String, String]]]()
  map1.put(topicPartition0, cRecordList0)
  map1.put(topicPartition1, cRecordList1)
  val cRecords1 = new ConsumerRecords(map1)

  val topicPartitions = new ArrayList[TopicPartition]()
  topicPartitions.add(topicPartition0)
  topicPartitions.add(topicPartition1)
}
