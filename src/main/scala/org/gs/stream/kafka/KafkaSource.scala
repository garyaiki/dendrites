package org.gs.stream.kafka

import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import akka.stream.scaladsl.Source
import java.util.Queue
import org.gs.kafka.WrappedConsumer

class KafkaSource[A, K, V](consumer: WrappedConsumer[A, K, V]) extends GraphStage[SourceShape[A]]{

  val out = Outlet[A]("KafkaSource")
  override val shape: SourceShape[A] = SourceShape(out)
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      private var offset = 0
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          consumer.next() match {
            case Some(x) => push(out, x)
            case _ =>
          }
        }
      })
    }
  }
}
