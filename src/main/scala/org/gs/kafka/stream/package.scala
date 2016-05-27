/**
  */
package org.gs.kafka

import akka.NotUsed
import akka.stream.scaladsl.{Flow, UnzipWith, UnzipWith2, UnzipWithApply}
import scala.collection.JavaConverters._
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords}
import org.apache.kafka.common.TopicPartition
import scala.collection.immutable.Queue
import scala.collection.mutable.ArrayBuffer

/** Classes for KafkaSource, KafkaSink, ConsumerRecord queue stages. Also Flows and functions for
  * Kafka
  *
  * @author Gary Struthers
  * @see [[http://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/consumer/ConsumerRecords.html "ConsumerRecords"]]
  * @see [[http://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/consumer/ConsumerRecord.html "ConsumerRecord"]]
  */
package object stream {

  /** ConsumerRecords provides an iterator, for each ConsumerRecord queue it
    *
    * @tparam K key type
    * @tparam V value type
    * @param records ConsumerRecords returned from Kafka consumer poll
    * @return records in a queue
    */
  def queueRecords[K, V](it: Iterator[ConsumerRecord[K, V]]): Queue[ConsumerRecord[K, V]] = {
    val queue = Queue.newBuilder[ConsumerRecord[K, V]]
    while(it.hasNext) {
      queue += it.next()
    }
    queue.result()      
  }

  /** KafkaConsumer poll returns ConsumerRecords, extract each ConsumerRecord and queue them. The
    * queue is used in Akka Streams to pull 1 ConsumerRecord at a time. Use this when there is one
    * topicPartition or when multiple topicPartitions are processed as one
    *
    * @tparam K key type
    * @tparam V value type
    * @param records ConsumerRecords returned from Kafka consumer poll
    * @return records in a queue
    */
  def extractRecords[K, V](records: ConsumerRecords[K,V]): Queue[ConsumerRecord[K, V]] = {
    System.out.println(s"extractRecords:${records.count()}")
    val it = records.iterator().asScala
    queueRecords[K, V](it)
  }

  /** Flow to map ConsumerRecords to a Queue of ConsumerRecord. This allows a Stream to pull
    * one ConsumerRecord at a time 
    *
    * @see [[org.gs.examples.account]]
    * @example [[org.gs.kafka.stream.ConsumerRecordsFlowSpec]]
    *
    * @tparam K key type
    * @tparam V value type
    * @return a queue of ConsumerRecord
    */
  def consumerRecordsFlow[K, V]: Flow[ConsumerRecords[K, V], Queue[ConsumerRecord[K, V]], NotUsed] =
          Flow[ConsumerRecords[K, V]].map(extractRecords[K, V])

  /** Like extract records but unzips them into 2 queues of ConsumerRecord, separated by partition
    * Use this when there are 2 topic partitions and they should be processed separately
    *
    * @tparam K key type
    * @tparam V value type
    * @param records ConsumerRecords returned from Kafka consumer poll
    * @return tuple2 of queue of ConsumerRecord
    */
  def unzipRecords[K, V](records: ConsumerRecords[K,V]): (Queue[ConsumerRecord[K, V]],
          Queue[ConsumerRecord[K, V]]) = {
    val partitions = records.partitions()
    require(partitions.size() == 2)
    val buff = new ArrayBuffer[TopicPartition](2)
    val it = partitions.iterator()
    while(it.hasNext()) {
      buff += it.next()
    }
    val l0 = records.records(buff(0))
    val ab0 = l0.asScala    
    val l1 = records.records(buff(1))
    val ab1 = l1.asScala
    (queueRecords(ab0.iterator), queueRecords(ab0.iterator))
  }

  /** Map a ConsumerRecord to just its value */
  def extractValue[K, V](record: ConsumerRecord[K,V]): V = {
    System.out.println(s"extractValue offset:${record.offset()}")
    record.value()
  }

  /** Flow to Map a ConsumerRecord to just its value */
  def consumerRecordValueFlow[K, V]: Flow[ConsumerRecord[K, V], V, NotUsed] =
        Flow[ConsumerRecord[K, V]].map(extractValue[K, V])
}
