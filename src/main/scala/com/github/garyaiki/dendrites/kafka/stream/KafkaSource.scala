/**
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
package com.github.garyaiki.dendrites.kafka.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{ActorAttributes, Attributes, Outlet, SourceShape, Supervision}
import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler, TimerGraphStageLogic}
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.consumer.{CommitFailedException, Consumer, ConsumerRecords, InvalidOffsetException}
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.errors.{AuthorizationException, WakeupException}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.control.NonFatal
import com.github.garyaiki.dendrites.concurrent.calculateDelay
import com.github.garyaiki.dendrites.kafka.ConsumerConfig

/** Source stage that reads from Kafka
  *
  * KafkaSource calls KafkaConsumer.poll() which reads all available messages into ConsumerRecords
  * if not empty it's pushed to the next stage. KafkaSource receives onPull when stream starts and
  * when all messages in the last poll() have been processed.
  *
  * Kafka commitSync was meant to confirm that messages have been read. But in an Akka Stream it
  * can confirm messages have been processed. If there is a thrown exception or timeout commitSync
  * won't be called. So messages that weren't committed will be re-polled.
  *
  * To use commitSync() this way, set Kafka server.properties enable.auto.commit=false
  *
  * Do Not add a consumer to a consumer group while uncommitted messages are being processed. This
  * can cause a rebalancing defeating this trick.
  *
  * The stream containing KafkaSource and all stages in the stream must use Supervision.Stop (the
  * default) to ensure Kafka doesn't erroneously commit after an exception.
  *
  * KafkaConsumer is single threaded and is created and closed with the stream. It's poll and commit
  * block, Akka Stream's default dispatcher may run out of threads, use a blocking dispatcher
  * {{{
  *   val dispatcher = ActorAttributes.dispatcher("dendrites.blocking-dispatcher")
  *
  *   val kafkaSource = KafkaSource[String, Array[Byte]](accountConsumerConfig).withAttributes(dispatcher)
  * }}}
  *
  * @tparam K Kafka key
  * @tparam V Kafka value
  * @param consumerConfig a consumer,or mock consumer, factory with properties, topics, timeout
  * @param logger implicit LoggingAdapter
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
    * @constructor sets up Supervision with exponential backoff
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new TimerGraphStageLogic(shape) {

      private def decider = inheritedAttributes.get[SupervisionStrategy].map(_.decider).
        getOrElse(Supervision.stoppingDecider)

      var kafkaConsumer: Consumer[K, V] = null
      var retries = 0
      val maxDuration = consumerConfig.maxDuration
      val curriedDelay = consumerConfig.curriedDelay
      var waitForTimer: Boolean = false
      var needCommit = false

      override def preStart(): Unit = kafkaConsumer = consumerConfig.createAndSubscribe()

      def myHandler(): Unit = {
        if(!waitForTimer) {
          try {
            if(needCommit) {
              kafkaConsumer commitSync() // blocking
              needCommit = false
            }
            val records = kafkaConsumer poll(consumerConfig.timeout) // blocking
            if(!records.isEmpty) { // don't push if no record available
              push(out, records)
              needCommit = true
            } else logger.debug("KafkaSource records isEmpty {}", records.isEmpty)
            retries = 0
          } catch {
            case NonFatal(e) => decider(e) match {
              case Supervision.Stop | Supervision.Restart => failStage(e)

              case Supervision.Resume => {
                val duration = curriedDelay(retries)
                if(duration < maxDuration) {
                  waitForTimer = true
                  scheduleOnce(None, duration)
                } else {
                  failStage(e) // too many retries
                }
              }
            }
          }
        }
      }

      setHandler(out, new OutHandler { override def onPull(): Unit = myHandler() })

      override protected def onTimer(timerKey: Any): Unit = {
        retries += 1
        waitForTimer = false
        myHandler()
      }

      override def postStop(): Unit = {
        if(needCommit) {
          kafkaConsumer commitSync() // blocking
        }
        kafkaConsumer close()
      }
    }
  }
}

/** Create a configured Kafka Source that is subscribed to topics and has a Supervision Strategy */
object KafkaSource {

  /** Create Kafka Sink as Akka Sink subscribed to configured topic with Supervision
    * @tparam K key type
    * @tparam V value type
    * @param consumer configuration object
    * @param implicit logger
    * @return Source[ConsumerRecords[K, V], NotUsed]
    */
  def apply[K, V](consumer: ConsumerConfig[K, V])(implicit ec: ExecutionContext, logger: LoggingAdapter):
    Source[ConsumerRecords[K, V], NotUsed] = {

    val source = Source.fromGraph(new KafkaSource[K, V](consumer))
    source.withAttributes(ActorAttributes.supervisionStrategy(decider))
  }

  /** Supervision strategy
    *
    * @see [[http://kafka.apache.org/0101/javadoc/org/apache/kafka/clients/consumer/CommitFailedException.html CommitFailedException]]
    * @see [[http://kafka.apache.org/0101/javadoc/org/apache/kafka/common/errors/WakeupException.html WakeupException]]
    * @see [[http://kafka.apache.org/0101/javadoc/org/apache/kafka/common/errors/AuthorizationException.html AuthorizationException]]
    * @see [[http://kafka.apache.org/0101/javadoc/org/apache/kafka/clients/consumer/InvalidOffsetException.html InvalidOffsetException]]
    * @see [[http://kafka.apache.org/0101/javadoc/org/apache/kafka/common/KafkaException.html KafkaException]]
    */
  def decider: Supervision.Decider = {
    case _: CommitFailedException => Supervision.Resume // Can't commit current poll
    case _: WakeupException => Supervision.Resume // poll interrupted
    case _: AuthorizationException => Supervision.Stop
    case _: InvalidOffsetException => Supervision.Stop
    case _: KafkaException => Supervision.Stop // Catch all for Kafka exceptions
    case _  => Supervision.Stop
  }
}
