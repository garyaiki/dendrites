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

/** Source that polls Kafka. When it receives a pull from downstream it checks if the previous poll
  * needs to be commited,
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

  var kafkaConsumer: Consumer[K, V] = null


  /** commitSync blocks and fails if session timesout or partitions have been rebalanced,
    * enable.auto.commit must be set false in broker properties
    */
  def doCommitSync() {
    try {
      logger.debug("KafkaSource before doCommitSync")
      kafkaConsumer.commitSync()
      logger.debug("KafkaSource after doCommitSync")
    } catch {
       case e: WakeupException => {
         doCommitSync()
         logger.error(e, e.getMessage)
         throw e
       }
       case e: CommitFailedException => {
         logger.error(e, e.getMessage)
         throw e         
       }
       case NonFatal(e) => {
         logger.error(e, e.getMessage)
         throw e          
       }
    }
  }

  val out = Outlet[ConsumerRecords[K, V]](s"KafkaSource")
  override val shape = SourceShape(out)

  /** When downstream pulls check if messages from last poll need to be committed, commitSync blocks
    * and can fail if session has timedout or partitions have been rebalanced. Then poll Kafka, this
    * also blocks. If poll returns an empty ConsumerRecords do nothing, if there are records polled
    * push ConsumerRecords to the next stage
    * 
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      override def preStart(): Unit = {
        logger.debug("KafkaSource preStart")
        kafkaConsumer = consumerConfig.createConsumer()
      }

      private var needCommit = false
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          logger.debug("KafkaSource onPull")
          if(needCommit) doCommitSync()
          val records = kafkaConsumer.poll(consumerConfig.timeout) //blocks
          logger.debug("KafkaSource records count:${}", records.count())
          if(!records.isEmpty()) { // don't push if no record available
            push(out, records)
            needCommit = true
          }
        }
      })
      
      override def postStop(): Unit = {
        kafkaConsumer.commitSync()
        kafkaConsumer.close()
        logger.debug("KafkaSource postStop")
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