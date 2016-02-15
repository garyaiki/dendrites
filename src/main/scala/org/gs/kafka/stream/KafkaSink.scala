package org.gs.kafka.stream

import akka.stream.{Attributes, Inlet, SinkShape}
import akka.stream.scaladsl.Sink
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler}
import org.gs.kafka.WrappedProducer
/** Custom Sink that writes to Kafka
  *  
  * @author Gary Struthers
  * @tparam <A> Type received from stream
  * @tparam <K> Kafka ProducerRecord key
  * @tparam <V> Kafka ProducerRecord value, may be the same or different than A
  */
class KafkaSink[A, K, V](producer: WrappedProducer[A, K, V]) extends GraphStage[SinkShape[A]]{
  val in = Inlet[A](s"KafkaSink")
  override val shape: SinkShape[A] = SinkShape(in)
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      private var offset = 0
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val item = grab(in)
          producer.send(item)
        }
      })
    }
  }
}

object KafkaSink {
  def apply[A, K, V](producer: WrappedProducer[A, K, V]): Sink[A, Unit] = {
    Sink.fromGraph(new KafkaSink[A, K, V](producer))
  }
}
