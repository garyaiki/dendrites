package org.gs.kafka.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.consumer.{Consumer, ConsumerRecords}
import org.gs.kafka.ConsumerConfig

/** Source stage that reads from Kafka
  *
  * KafkaSource calls KafkaConsumer.poll() which reads all available messages into a ConsumerRecords
  * if it's not empty it's pushed to the next stage. KafkaSource receives an onPull when the stream
  * starts and when all messages in the last poll() have been processed. This uses KafkaConsumer's
  * commitSync after all messages from the last poll() have been processed.
  *
  * Kafka commitSync was meant to confirm that messages have been read. But in an Akka Stream it
  * can confirm all messages have been processed. If there is a thrown exception or a timeout
  * commitSync won't be called. So messages that weren't committed will be retried.
  *
  * To use commitSync() this way, in Kafka server.properties enable.auto.commit=false
  *
  * Do Not add a consumer to a consumer group while uncommitted messages are being processed. This
  * can cause a rebalancing defeating this trick.
  *
  * KafkaConsumer is single threaded and is created and closed with the stream, as opposed to
  * KafkaProducer which should be reused by other streams and processes.
  *
  * @tparam K Kafka key
  * @tparam V Kafka value
  * @param consumerConfig a consumer,or mock consumer, factory with properties, topics, timeout
  * @author Gary Struthers
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
        kafkaConsumer = consumerConfig.createAndSubscribe()
      }

      private var needCommit = false
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          if(needCommit) {
            kafkaConsumer commitSync() // blocking
            needCommit = false
          }
          val records = kafkaConsumer poll(consumerConfig.timeout) // blocking
          if(!records.isEmpty()) { // don't push if no record available
            push(out, records)
            needCommit = true
          } else logger.debug("KafkaSource records isEmpty {}", records.isEmpty())
        }
      })

      override def postStop(): Unit = {
        if(needCommit) {
          kafkaConsumer commitSync() // blocking
        }
        kafkaConsumer close()
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
