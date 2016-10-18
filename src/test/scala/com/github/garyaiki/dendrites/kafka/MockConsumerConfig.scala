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
package com.github.garyaiki.dendrites.kafka

import java.lang.{Long => JLong}
import java.util.{ArrayList, List => JList}
import org.apache.kafka.clients.consumer.{Consumer, MockConsumer, OffsetResetStrategy}
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener
import org.apache.kafka.common.TopicPartition
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import scala.collection.JavaConverters._
import scala.collection.mutable.HashMap
import com.github.garyaiki.dendrites.concurrent.calculateDelay

/** Create MockConsumer, initilize with test topics, partitions, and ConsumerRecords. Subscribe to
  * test topics
  *
  * @author Gary Struthers
 */
object MockConsumerConfig extends ConsumerConfig[String, String] with MockConsumerRecords {
  val props = null
  val config = null
  val topics = List(topic).asJava
  val timeout = 1000L
  val minDuration = FiniteDuration(100, MILLISECONDS)
  val maxDuration = FiniteDuration(1000, MILLISECONDS)
  val randomFactor = 0.2
  val curriedDelay = calculateDelay(minDuration, maxDuration, 0.2) _
  
  def createAndSubscribe(): Consumer[Key, Value] = {
    val mc = new MockConsumer[Key, Value](OffsetResetStrategy.EARLIEST)
    mc.subscribe(topics, new NoOpConsumerRebalanceListener())
    mc.rebalance(topicPartitions)
    val beginningOffsets = new HashMap[TopicPartition, JLong]()
    beginningOffsets.put(topicPartition0, 0L)
    beginningOffsets.put(topicPartition1, 0L)
    mc.updateBeginningOffsets(beginningOffsets.asJava)
    mc.seekToBeginning(topicPartitions)
    val it0 = cRecordList0.iterator()
    while (it0.hasNext()) mc.addRecord(it0.next())
    val it1 = cRecordList1.iterator()
    while (it1.hasNext()) mc.addRecord(it1.next())
    mc
  }
}
