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
import akka.stream.{ActorAttributes, Attributes, Inlet, SinkShape, Supervision}
import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.scaladsl.Sink
import akka.stream.stage.{AsyncCallback, GraphStage, GraphStageLogic, InHandler, TimerGraphStageLogic}
import org.apache.kafka.clients.producer.{Callback, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.KafkaException
// Retriable Kafka exceptions
import org.apache.kafka.common.errors.{CorruptRecordException, InvalidMetadataException,
  NotEnoughReplicasAfterAppendException, NotEnoughReplicasException, OffsetOutOfRangeException, TimeoutException,
  UnknownTopicOrPartitionException, RetriableException}
// Stopping Kafka exceptions
import org.apache.kafka.common.errors.{InvalidTopicException, OffsetMetadataTooLarge, RecordBatchTooLargeException,
  RecordTooLargeException, UnknownServerException}
import scala.util.control.NonFatal
import com.github.garyaiki.dendrites.kafka.ProducerConfig
import com.github.garyaiki.dendrites.kafka.stream.KafkaSink.decider

/** A copy of [[com.github.garyaiki.dendrites.kafka.stream.KafkaSink]] modified to inject exceptions into Kafka Producer
  * asynchronous callback
  *
  * @tparam K Kafka ProducerRecord key
  * @tparam V Type of serialized object received from stream and Kafka ProducerRecord value
  * @param wProd extends KafkaProducer with key, value, and topic fields
  * @param testException for injecting exceptions to test Supervision
  * @param implicit logger
  *
  * @author Gary Struthers
  *
  */
class MockKafkaSink[K, V](prod: ProducerConfig[K, V], testException: RuntimeException = null)
  (implicit logger: LoggingAdapter) extends GraphStage[SinkShape[V]] {

  val producer = prod.producer
  /** for access to MockProducer debugging methods
  import org.apache.kafka.clients.producer.MockProducer
  val mockProducer = producer match {
    case x: MockProducer[K, V] => x
  }*/
  val in = Inlet[V](s"MockKafkaSink")
  override val shape: SinkShape[V] = SinkShape(in)
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new TimerGraphStageLogic(shape) {

      private def decider = inheritedAttributes.get[SupervisionStrategy].map(_.decider).
        getOrElse(Supervision.stoppingDecider)

      var retries = 0
      val maxDuration = prod.maxDuration
      val curriedDelay = prod.curriedDelay
      var waitForTimer: Boolean = false

      /** pull initializes stream requests */
      override def preStart(): Unit = pull(in)

      /** exception handler for producer send's callback */
      def asyncExceptions(pRecord: ProducerRecord[K, V], callback: Callback)(e: Exception): Unit = {
        e match {
          case NonFatal(e) => decider(e) match {
              case Supervision.Stop => {
                logger.error(e, "MockKafkaSink Stop ex:{}", e.getMessage)
                failStage(e)
              }
              case Supervision.Resume => {
                val duration = curriedDelay(retries)
                if(duration < maxDuration) {
                  waitForTimer = true
                  scheduleOnce((pRecord, callback), duration)
                } else {
                  logger.error(e, "MockKafkaSink Resume ex:{} duration:{}", e.getMessage, duration)
                  failStage(e) // too many retries
                }
              }
          }
        }
      }

      /** send ProducerRecord to Kafka with callback, on success pull from upstream, on failure
        * retry on RetryableException, log and throw other exceptions
        */
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          if(!waitForTimer) {
            val item = grab(in)
            val producerRecord = new ProducerRecord[K, V](prod.topic, prod.generateKey(), item)
            var errorCallback: AsyncCallback[Exception] = null
            val pullCallback = getAsyncCallback{ (_: Unit) => pull(in) }
            val kafkaCallback = new Callback() {
              def onCompletion(meta: RecordMetadata, e: Exception): Unit = {
                if (e != null) errorCallback invoke(e) else
                  if (testException != null) errorCallback invoke(testException) else pullCallback invoke((): Unit)
              }
            }
            val curriedAsyncEx = asyncExceptions(producerRecord, kafkaCallback) _
            errorCallback = getAsyncCallback(curriedAsyncEx)
            producer send(producerRecord, kafkaCallback)
          }
        }
      })

      override protected def onTimer(timerKey: Any): Unit = {
        retries += 1
        waitForTimer = false
        timerKey match {
          case (pRec: ProducerRecord[K, V], callback: Callback) => producer send(pRec, callback)
          case x => throw new IllegalArgumentException(
            s"expected (ProducerRecord[K, V], Callback)) received:${x.toString()}")
        }
      }
    }
  }
}

/** Factory for KafkaSink with wrapped KafkaProducer and its properties configuration
  * Sink.fromGraph promotes KafkaSink from a SinkShape to a Sink
  */
object MockKafkaSink {

  /** Create MockKafkaSink as Akka Sink with KafkaSink Supervision
    *
    * @tparam K key type
    * @tparam V value type
    * @param producer configuration object
     * @param implicit logger
    * @return Sink[V, NotUsed]
    */
  def apply[K, V](producer: ProducerConfig[K, V], testException: RuntimeException)(implicit logger: LoggingAdapter):
      Sink[V, NotUsed] = {

    val sink = Sink.fromGraph(new MockKafkaSink[K, V](producer, testException))
    sink.withAttributes(ActorAttributes.supervisionStrategy(decider))
  }
}
