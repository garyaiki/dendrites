package org.gs.stream.kafka

import akka.stream.{Attributes, Inlet, SinkShape}
import akka.stream.scaladsl.Sink
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler}
import org.gs.kafka.WrappedProducer

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
