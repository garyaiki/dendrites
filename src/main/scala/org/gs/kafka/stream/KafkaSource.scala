package org.gs.kafka.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import akka.stream.scaladsl.Source
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.clients.consumer.{CommitFailedException, Consumer, ConsumerRecords, KafkaConsumer}
import scala.util.control.NonFatal
import org.gs.kafka.ConsumerConfig
import scala.collection.JavaConversions._
import org.apache.kafka.common.PartitionInfo

/** KafkaSource calls KafkaConsumer.poll() which reads all available messages into a ConsumerRecords
  * if it's not empty it's pushed to the next stage. KafkaSource receives an onPull when the stream
  * starts and when all messages in the last poll() have been processed. This uses KafkaConsumer's
  * commitSync after all messages from the last poll() have been processed. This adds resilliency.
  * Kafka commitSync was meant to confirmed that messages have been read. But in an Akka Stream it's
  * purpose can expand to confirm all messages in the stream have been processed. A thrown exception
  * or a timeout means commitSync won't be called. This means the messages that weren't committed
  * will be retried after the next poll().
  * 
  * To use commitSync() this way enable.auto.commit must be set false in Kafka server.properties
  * Do Not add a consumer to a consumer group while uncommitted messages are being processed. This
  * can cause a rebalancing
  *
  * KafkaConsumer is single threaded and is created and closed with the stream, as opposed to
  * KafkaProducer which is meant to have the server's lifetime.
  *
  * @author Gary Struthers
  *
  * @tparam <K> Kafka key
  * @tparam <V> Kafka value
  * @param consumer a wrapped KafkaConsumer Java client that is configured and subscribed to topics
  * or it wraps a MockConsumer for testing
  */
class KafkaSource[K, V](val consumerConfig: ConsumerConfig[K, V])(implicit logger: LoggingAdapter)
    extends GraphStage[SourceShape[ConsumerRecords[K, V]]]{

  val out = Outlet[ConsumerRecords[K, V]](s"KafkaSource")
  override val shape = SourceShape(out)

  /** On downstream pull check if messages from last poll need to be committed, commitSync() blocks.
    * Then poll Kafka, this also blocks. If poll returns an empty ConsumerRecords do nothing, if it
    * contains records, push ConsumerRecords to the next stage
    * 
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      var kafkaConsumer: Consumer[K, V] = null

      override def preStart(): Unit = {
        kafkaConsumer = consumerConfig.createConsumer()
      }

      private var needCommit = false
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          if(needCommit) {
            kafkaConsumer.commitSync() //blocks
            needCommit = false
          }
          val records = kafkaConsumer.poll(consumerConfig.timeout) //blocks
          if(!records.isEmpty()) { // don't push if no record available
            push(out, records)
            needCommit = true
          }
        }
      })
      
      override def postStop(): Unit = {
        if(needCommit) {
          kafkaConsumer.commitSync() //blocks
        }
        kafkaConsumer.close()
      }
    }
  }
}

/** Create a configured Kafka Source that is subscribed to topics */
object KafkaSource {
  def apply[K, V](consumer: ConsumerConfig[K, V])(implicit logger: LoggingAdapter):
        Source[ConsumerRecords[K, V], NotUsed] = {
    Source.fromGraph(new KafkaSource[K, V](consumer))
  }
}