package org.gs.kafka

import java.lang.{Long => JLong}
import org.apache.kafka.clients.consumer.{Consumer, MockConsumer}
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import scala.collection.mutable.HashMap
import scala.collection.JavaConverters._
import org.gs._

object MockConsumerFacade extends ConsumerFacade[String, String] with MockConsumerRecords {
  val props = null
  val config = null
  val topics = List(topic).asJava
  val timeout = 1000L

  override def apply(): Consumer[Key, Value] = {
    val mc = new MockConsumer[Key, Value](OffsetResetStrategy.EARLIEST)
    mc.subscribe(topics)
    val beginningOffsets = new HashMap[TopicPartition, JLong]()
    beginningOffsets.put(topicPartition0, 0L)
    beginningOffsets.put(topicPartition1, 0L)
    mc.updateBeginningOffsets(beginningOffsets.asJava)
    mc.seekToBeginning(topicPartition0, topicPartition1)
    val it0 = cRecordList0.iterator()
    while (it0.hasNext()) mc.addRecord(it0.next())
    val it1 = cRecordList1.iterator()
    while (it1.hasNext()) mc.addRecord(it1.next())
    mc
  }
}
