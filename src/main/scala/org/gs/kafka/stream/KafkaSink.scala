package org.gs.kafka.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{Attributes, Inlet, SinkShape}
import akka.stream.scaladsl.Sink
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler}
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
class KafkaSink[K, V](producer: WrappedProducer[K, V])(implicit logger: LoggingAdapter)
      extends GraphStage[SinkShape[V]]{
  val in = Inlet[V](s"KafkaSink")
  override val shape: SinkShape[V] = SinkShape(in)
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      private var offset = 0
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val item = grab(in)
          producer.send(item) match {
            case Left(message) => logger.warning(message)
            case Right(meta) => logger.debug("Send OK offset:{} partition:{} topic:{}",
                meta.offset(), meta.partition, meta.topic) 
          }
        }
      })
    }
  }
}

/** Create a configured Kafka Sink with wrapped KafkaProducer and its properties configuration
  * Sink.fromGraph promotes KafkaSink from a SinkShape to a Sink 
  */
object KafkaSink {
  def apply[K, V](producer: WrappedProducer[K, V])(implicit logger: LoggingAdapter):
        Sink[V, NotUsed] = {
    Sink.fromGraph(new KafkaSink[K, V](producer))
  }
}
