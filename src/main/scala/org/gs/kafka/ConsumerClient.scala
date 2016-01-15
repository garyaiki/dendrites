package org.gs.kafka

import java.util.{ List => JList, Properties, Queue }
import org.apache.kafka.clients.consumer.{ ConsumerRecord, KafkaConsumer}
import scala.collection.JavaConverters._
import org.gs._

/** KafkaConsumer client
  *
  * This calls Kafka's Java client, its poll method returns ConsumerRecords, a collection of
  * ConsumerRecord. Each record is transferred to a subclass of Java Queue, such as
  * @see https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentLinkedQueue.html
  * @note consumer isn't thread safe but the queue can be
  * @note enable.auto.commit should be set false then commitSync called after record is processed
  * @note consumer closes if poll isn't called within a session timeout period commitSync then fails 
  * @note call close when done
  * @see https://kafka.apache.org/090/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html
  * @tparam <K> key type of ConsumerRecord
  * @tparam <V> value type of ConsumerRecord
  * @param queue for ConsumerRecord
  * @param propsName name of .properties in classpath, used by KafkaConsumer
  * @param topics Java List of topic names
  * @param timeout millis for poll to wait for data, 0 returns immediately
  */
class ConsumerClient[K, V](queue: Queue[V],
    props: Properties,
    topics: JList[String],
    timeout: Long = 0) {

  val consumer = new KafkaConsumer[K, V](props)
  consumer.subscribe(topics)

  def poll(): Unit = {
    val records = consumer.poll(timeout)
    val sr = records.asScala
    def enqueueValue(cr: ConsumerRecord[K, V]): Unit = { queue.offer(cr.value()) }
    sr.foreach(enqueueValue)
  }

  def next(): Option[V] = {
    if (queue.isEmpty) None else {
      val element = Option(queue.poll())
      element match {
        case Some(x) => Some(x)
        case _ => None
      }
    }
  }
}
