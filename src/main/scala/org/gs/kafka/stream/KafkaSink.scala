package org.gs.kafka.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{ Attributes, Inlet, SinkShape }
import akka.stream.scaladsl.Sink
import akka.stream.stage.{ AsyncCallback, GraphStage, GraphStageLogic, InHandler }
import org.apache.kafka.clients.producer.BufferExhaustedException
import org.apache.kafka.clients.producer.{ Callback, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.errors.RetriableException
import org.gs.kafka.WrappedProducer

/** KafkaSink is initialized with a wrapped KafkaProducer. The wrapper includes topic, key, and Key,
  * Value types specific to this topic. KafkaProducer is heavy weight and multi-threaded and usually
  * serves other topics and is long lived. If a Kafka RetryableException is thrown while writing
  * KafkaSink catches it and retries the write. If a write throws a subclass of KafkaException this
  * test's Decider stops the write stream.
  *
  * KafkaProducer's send() returns a Java/Guava listenable Future, not a nice Scala Future. Kafka's
  * listenable Future passes 2 arguments to a callback, a RecordMetadata which is null if an
  * exception was thrown and an Exception which is null if a non null RecordMetadata was returned?!
  * I cope with this in the least awful way I found. If a good RecordMetadata was returned it means
  * success so another callback is called that pulls from upstream. If an exception was returned yet
  * another callback is called and it deals with 2 types of exceptions. KafkaException is logged and
  * rethrown and it will be handled by a Supervision.Decider attached to the stream's
  * ActorMaterializer. RetriableException is when there was a fleeting error that may not happen
  * next time, so grab the same message and send it again. 
  *
  * @author Gary Struthers
  * @tparam <K> Kafka ProducerRecord key
  * @tparam <V> Type of serialized object received from stream and Kafka ProducerRecord value
  * @param producer is a KafkaProducer wrapped in an object that constructs it with configuration
  * properties.
  */
class KafkaSink[K, V](wrp: WrappedProducer[K, V])(implicit logger: LoggingAdapter)
    extends GraphStage[SinkShape[V]] {

  val producer = wrp.producer
  val in = Inlet[V](s"KafkaSink")
  override val shape: SinkShape[V] = SinkShape(in)
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      /** start backpressure in custom Sink */
      override def preStart(): Unit = {
        pull(in)
      }

      def asyncExceptions(producerRecord: ProducerRecord[K, V], callback: Callback)(e: Exception): Unit = {
        e match {
          case e: RetriableException => {
            logger.error(e, "Kafka send retryable exception, attempt retry {}", e.getMessage)
              producer.send(producerRecord, callback)          
          }
          case e: KafkaException => {
            logger.error(e, "Kafka send un-retryable exception,  {}", e.getMessage)
            throw(e)
          }
        }
      }

      def asyncPull(rm: RecordMetadata): Unit = {
        pull(in)
      }

      val pullCallback = getAsyncCallback(asyncPull)

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val item = grab(in)
          val producerRecord = new ProducerRecord[K, V](wrp.topic, wrp.key, item)
          var errorCallback: AsyncCallback[Exception] = null
          val kafkaCallback = new Callback() {
            def onCompletion(meta: RecordMetadata, e: Exception): Unit = {
              if (e != null) errorCallback.invoke(e) else pullCallback.invoke(meta)
            }
          }
          val curriedAsyncEx = asyncExceptions(producerRecord, kafkaCallback) _
          errorCallback = getAsyncCallback(curriedAsyncEx)
          producer.send(producerRecord, kafkaCallback)
        }
      })

    }
  }
}

/** Create a configured Kafka Sink with wrapped KafkaProducer and its properties configuration
  * Sink.fromGraph promotes KafkaSink from a SinkShape to a Sink
  */
object KafkaSink {
  def apply[K, V](producer: WrappedProducer[K, V])(implicit logger: LoggingAdapter): Sink[V, NotUsed] = {
    Sink.fromGraph(new KafkaSink[K, V](producer))
  }
}
