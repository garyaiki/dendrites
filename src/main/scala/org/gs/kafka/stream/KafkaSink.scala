package org.gs.kafka.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{Attributes, Inlet, SinkShape}
import akka.stream.scaladsl.Sink
import akka.stream.stage.{AsyncCallback, GraphStage, GraphStageLogic, InHandler}
import org.apache.kafka.clients.producer.{Callback, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.errors.RetriableException
import org.gs.kafka.ProducerConfig

/** Sink stage that writes to Kafka
  *
  * KafkaSink is initialized with a wrapped KafkaProducer. It includes topic, key, and Key, Value
  * types specific to the topic. KafkaProducer is heavy weight, multi-threaded, usually serves other
  * topics and is long lived. If a Kafka RetryableException is thrown while writing, KafkaSink
  * catches it and retries the write. If a write throws a subclass of KafkaException this
  * test's Decider stops the write stream.
  *
  * KafkaProducer's send() returns a Java/Guava ListenableFuture, not a nice Scala Future. Kafka's
  * ListenableFuture passes 2 arguments to a Kafka Producer Callback, a RecordMetadata and an
  * Exception. One of these arguments will be null. I cope with this in the least awful way I found.
  * If a RecordMetadata was returned it means success so an AsynCallback is called that pulls a
  * record from upstream. If an exception was returned a different AsynCallback is called that
  * deals with 2 types of exceptions, RetriableException and KafkaException.
  *
  * RetriableException is when there was a fleeting error that may not recur so resend the message.
  * This will keep retrying indefinitely until stream or other container times out.
  *
  * A KafkaException is logged and rethrown.
  *
  * @tparam K Kafka ProducerRecord key
  * @tparam V Type of serialized object received from stream and Kafka ProducerRecord value
  * @param wProd extends KafkaProducer with key, value, and topic fields
  *
  * @author Gary Struthers
  *
  */
class KafkaSink[K, V](wProd: ProducerConfig[K, V])(implicit logger: LoggingAdapter)
    extends GraphStage[SinkShape[V]] {

  val producer = wProd.producer
  val in = Inlet[V](s"KafkaSink")
  override val shape: SinkShape[V] = SinkShape(in)
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      
      /** pull initializes stream requests */
      override def preStart(): Unit = {
        pull(in)
      }

      /** exception handler for producer send's callback */
      def asyncExceptions(pRecord: ProducerRecord[K, V], callback: Callback)(e: Exception): Unit = {
        e match {
          case e: RetriableException => {
            logger debug("Kafka send retryable exception, attempt retry {}", e.getMessage)
              producer send(pRecord, callback)
          }
          case e: KafkaException => {
            logger error(e, "Kafka send un-retryable exception,  {}", e.getMessage)
            throw(e)
          }
        }
      }

      /** send ProducerRecord to Kafka with callback, on success pull from upstream, on failure
        * retry on RetryableException, log and throw other exceptions
        */
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val item = grab(in)
          val producerRecord = new ProducerRecord[K, V](wProd.topic, wProd.key, item)
          var errorCallback: AsyncCallback[Exception] = null
          val pullCallback = getAsyncCallback{ (_: Unit) => pull(in) }
          val kafkaCallback = new Callback() {
            def onCompletion(meta: RecordMetadata, e: Exception): Unit = {
              if (e != null) errorCallback invoke(e) else pullCallback invoke((): Unit)
            }
          }
          val curriedAsyncEx = asyncExceptions(producerRecord, kafkaCallback) _
          errorCallback = getAsyncCallback(curriedAsyncEx)
          producer send(producerRecord, kafkaCallback)
        }
      })
    }
  }
}

/** Factory for KafkaSink with wrapped KafkaProducer and its properties configuration
  * Sink.fromGraph promotes KafkaSink from a SinkShape to a Sink
  */
object KafkaSink {
  def apply[K, V](producer: ProducerConfig[K, V])(implicit logger: LoggingAdapter):
          Sink[V, NotUsed] = {
    Sink.fromGraph(new KafkaSink[K, V](producer))
  }
}
