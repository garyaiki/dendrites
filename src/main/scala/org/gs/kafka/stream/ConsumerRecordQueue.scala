package org.gs.stream.kafka

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
//import akka.stream.scaladsl.Source
import org.apache.kafka.clients.consumer.ConsumerRecord
import scala.collection.immutable.Queue


class ConsumerRecordQueue[K, V]() extends
    GraphStage[FlowShape[Queue[ConsumerRecord[K, V]], ConsumerRecord[K, V]]] {

  val in = Inlet[Queue[ConsumerRecord[K, V]]] ("QueueConsumerRecord.in")
  val out = Outlet[ConsumerRecord[K, V]]("ConsumerRecord.out")
  override val shape = FlowShape.of(in, out)
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      private var q: Queue[ConsumerRecord[K, V]] = null

      def doQ(queue: Queue[ConsumerRecord[K, V]]): Unit = {
          if(q.isEmpty) pull(in) else {
            val (rec, tail) = queue.dequeue
            q = tail
            push(out, rec)
          }
        }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val inQ = grab(in)
          if(!q.isEmpty) doQ(inQ)
        }
      })
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          if(q.isEmpty) pull(in) else doQ(q)
        }
      })
    }
  }
}
