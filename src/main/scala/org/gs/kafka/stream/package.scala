/**
  */
package org.gs.stream

import akka.stream.scaladsl.{Flow, UnzipWith, UnzipWith2, UnzipWithApply}
import scala.collection.JavaConverters._
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords}
import org.apache.kafka.common.TopicPartition
import scala.collection.immutable.Queue
import scala.collection.mutable.ArrayBuffer

/** Akka Stream Flows for Kafka
  *
  * @author Gary Struthers
  * @see [[http://kafka.apache.org/090/javadoc/org/apache/kafka/clients/consumer/ConsumerRecords.html]]
  * @see [[http://kafka.apache.org/090/javadoc/org/apache/kafka/clients/consumer/ConsumerRecord.html]]
  */
package object kafka {

    def queueRecords[K, V](it: Iterator[ConsumerRecord[K, V]]): Queue[ConsumerRecord[K, V]] = {
      val queue = Queue.newBuilder[ConsumerRecord[K, V]]
      while(it.hasNext) {
        queue += it.next()
      }
      queue.result()      
    }
    /** Copy records from all topic partitions to a queue
     *
     * @tparam K key type
     * @tparam V value type
     * @param records ConsumerRecords returned from Kafka consumer poll
     * @return records in a queue
     */
    def extractRecords[K, V](records: ConsumerRecords[K,V]): Queue[ConsumerRecord[K, V]] = {
      val it = records.iterator().asScala
      queueRecords[K, V](it)
    }

    def unzipRecords[K, V](records: ConsumerRecords[K,V]): (Queue[ConsumerRecord[K, V]], Queue[ConsumerRecord[K, V]]) = {
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

  /** Flow to extract a queue of ConsumerRecord 
    *
    * @see [[org.gs.examples.account]]
    * @example [[org.gs.stream.kafka.ConsumerRecordsFlowSpec]]
    *
    * @tparam K key type
    * @tparam V value type
    * @return records in a queue
    */
    def consumerRecordsFlow[K, V]: Flow[ConsumerRecords[K, V], Queue[ConsumerRecord[K, V]], Unit] =
            Flow[ConsumerRecords[K, V]].map(extractRecords[K, V])
}
