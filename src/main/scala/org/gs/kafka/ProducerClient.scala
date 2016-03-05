package org.gs.kafka

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import java.util.concurrent.{Future => JFuture}
import org.apache.kafka.clients.producer.{Callback, MockProducer, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.serialization.Serializer
import scala.concurrent.{ExecutionContext }
import scala.concurrent.blocking
import scala.util.{Failure, Success}
import scala.util.control.NonFatal

/** Scala wrapper for KafkaProducer, Kafka's Java client. The createProducer function constructs a
 *  KafkaProducer Java client initilized with its properties
  *
  *
  * @author Gary Struthers 
  * @note producer holds a large buffer that should be closed when finished
  * @see https://kafka.apache.org/090/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html
  * @tparam <K> key type of ProducerRecord
  * @tparam <V> value type of ProducerRecord
  * @param systemName name of ActorSystem used to get execution context
  * @param dispatcherName name of execution context for Scala and Java futures
  * @param propsName name of .properties in classpath, used by KafkaProducer
  */
class ProducerClient[K, V](systemName: String, dispatcherName: String, propsName: String) {
  implicit val system = ActorSystem(systemName)
  implicit val ec = system.dispatchers.lookup(systemName + "." + dispatcherName)
  val producer = createProducer[K, V](propsName)

  /** Send producer record to Kafka. The Kafka Java client send method returns a Java future that
    * blocks. To get around this the Java future is wrapped in a Scala future that has its own
    * execution context so blocking is in another thread.
    *
    * @see http://kafka.apache.org/090/javadoc/org/apache/kafka/clients/producer/ProducerRecord.html
    * @see http://kafka.apache.org/090/javadoc/org/apache/kafka/clients/producer/RecordMetadata.html
 		* @param producerRecord contains topic, key, value
 		* @param ec execution context for futures, inner Java future blocks
 		* @return record metadata for success, error message for failure
 */
  def send(producerRecord: ProducerRecord[K, V])(implicit ec: ExecutionContext):
    Either[String, RecordMetadata] = {
      var result: Either[String, RecordMetadata] = null
      System.out.println("before Java send Future")
      blocking {
        val javaFuture: JFuture[RecordMetadata] = producer.send(producerRecord)
        try {
          val recordMetadata = javaFuture.get
          Right(recordMetadata)
        } catch {
          case NonFatal(e) => {System.out.println(s"failed Java send Future ${e.printStackTrace()}")
            Left(e.getMessage)
          }
        }
      }
    }

  def send(producerRecord: ProducerRecord[K, V], callback: Callback): Unit = {
    producer.send(producerRecord, callback)
  }
}

object ProducerClient extends WrappedProducer[String, Array[Byte]] {



  val producer = null
  val config = ConfigFactory.load()
  val topic = config.getString("dendrites.kafka.account.topic")
  val key = config.getString("dendrites.kafka.account.key")

}
