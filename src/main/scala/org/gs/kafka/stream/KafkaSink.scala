package org.gs.kafka.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{ Attributes, Inlet, SinkShape }
import akka.stream.scaladsl.Sink
import akka.stream.stage.{ GraphStage, GraphStageLogic, InHandler }
import org.apache.kafka.clients.producer.BufferExhaustedException
import org.apache.kafka.clients.producer.{ Callback, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.errors.RetriableException
import org.gs.kafka.WrappedProducer

/** Sink that writes to Kafka. When a serialized object is pushed to the sink the wrapped
  * KafkaProducer sends it to Kafka. The ProducerClient wraps the send's blocking Java Future in a
  * Scala Future that has its own execution context so the sink doesn't block. The future's result
  * is returned as an Either[String, RecordMetada]. String is an error message, logged as warning,
  * RecordMetada has successful message offset, partition, and topic, logged as debug
  *
  * @author Gary Struthers
  * @tparam <K> Kafka ProducerRecord key
  * @tparam <V> Type of serialized object received from stream and Kafka ProducerRecord value
  * @param producer is a KafkaProducer wrapped in an object that constructs it with configuration
  * properties.
  */
class KafkaSink[K, V](wrp: WrappedProducer[K, V])(implicit logger: LoggingAdapter)
    extends GraphStage[SinkShape[V]] {
  logger.debug("KafkaSink constructor")

  val producer = wrp.producer
  val in = Inlet[V](s"KafkaSink")
  override val shape: SinkShape[V] = SinkShape(in)
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      override def preStart(): Unit = {
        logger.debug("KafkaSink preStart")
        pull(in)
      }

      logger.debug("KafkaSink createLogic")
        def asyncExceptions(e: Exception): Unit = {
          e match {
            case e: RetriableException => {
              logger.error(e, "Kafka send retryable exception, attempt retry {}", e.getMessage)
              val item = grab(in)
              val producerRecord = new ProducerRecord[K, V](wrp.topic, wrp.key, item)
              producer.send(producerRecord, kafkaCallback)          
            }
            case e: KafkaException => {
              logger.error(e, "Kafka send un-retryable exception,  {}", e.getMessage)
              throw(e)
            }
          }
        }
        val errorCallback = getAsyncCallback(asyncExceptions)

        def asyncPull(rm: RecordMetadata): Unit = {
          logger.debug("Send OK offset:{} partition:{} topic:{}",rm.offset, rm.partition, rm.topic)
          pull(in)
        }
        val pullCallback = getAsyncCallback(asyncPull)
        val kafkaCallback = new Callback() {
          def onCompletion(meta: RecordMetadata, e: Exception): Unit = {
            if (e != null) errorCallback.invoke(e) else pullCallback.invoke(meta)
          }
        }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          logger.debug("KafkaSink begin onPush")
          val item = grab(in)
          logger.debug("KafkaSink grabbed item:$item")
          val producerRecord = new ProducerRecord[K, V](wrp.topic, wrp.key, item)
          producer.send(producerRecord, kafkaCallback)

          /*
          val meta = producer.send(producerRecord).get(timeout, MILLISECONDS) //, callback)
          logger.info("Send OK offset:{} partition:{} topic:{}",
            meta.offset(), meta.partition, meta.topic)

          logger.info("KafkaSink end onPush")
          pull(in)
          */
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
