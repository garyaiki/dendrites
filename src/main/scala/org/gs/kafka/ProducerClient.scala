package org.gs.examples.account.kafka

import akka.actor.ActorSystem
import java.util.concurrent.{Future => JFuture}
import scala.concurrent.{ExecutionContext, Future }
import scala.concurrent.blocking
import scala.util.{Failure, Success}
import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata }
import org.gs.kafka.createProducer


/** KafkaProducer client
  *
  * This calls Kafka's Java client, its send method returns a Java future that blocks. A Scala
  * wraps it and uses an execution context that is configured in resource.conf.
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

  /** Send producer record to Kafka
    *
    * @see http://kafka.apache.org/090/javadoc/org/apache/kafka/clients/producer/ProducerRecord.html
    * @see http://kafka.apache.org/090/javadoc/org/apache/kafka/clients/producer/RecordMetadata.html
 		* @param producerRecord contains topic, key, value
 		* @param ec execution context for futures, inner Java future blocks
 		* @return record metadata for success, error message for failure
 */
def send(producerRecord: ProducerRecord[K, V])(implicit ec: ExecutionContext):
    Either[String, RecordMetadata] = {

    val scalaFuture = Future {
      blocking {
        val javaFuture: JFuture[RecordMetadata] = producer.send(producerRecord)
        javaFuture.get
      }
    }
    var rm: Either[String, RecordMetadata] = Left("")
    scalaFuture onComplete {
      case Success(x) => rm = Right(x)
      case Failure(e) => rm = Left(e.getMessage)
    }
    rm
  }
}