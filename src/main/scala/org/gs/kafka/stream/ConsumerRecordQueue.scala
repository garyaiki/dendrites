package org.gs.kafka.stream

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import org.apache.kafka.clients.consumer.ConsumerRecord
import scala.collection.immutable.Queue

/** Flow that takes a Queue of ConsumerRecord and deques one on pull
  *
  * Flow that uses a queue to send Kafka messages one at a time. A Queue[ConsumerRecord] is pushed
  * from upstream, when downstream pulls 1 ConsumerRecord is dequeued and pushed downstream
  * 
  * @tparam K Kafka ConsumerRecord key
  * @tparam V Kafka ConsumerRecord value
  *
  * @author Gary Struthers
  */
class ConsumerRecordQueue[K, V]() extends
    GraphStage[FlowShape[Queue[ConsumerRecord[K, V]], ConsumerRecord[K, V]]] {

  val in = Inlet[Queue[ConsumerRecord[K, V]]] ("QueueConsumerRecord.in")
  val out = Outlet[ConsumerRecord[K, V]]("ConsumerRecord.out")
  override val shape = FlowShape.of(in, out)

  /** When downstream pulls check if queue exists or is empty, pull a queue if it is. When the queue
    * has a ConsumerRecord, dequeue 1 and push it downstream
    * 
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      private var q: Queue[ConsumerRecord[K, V]] = null

      def doQ(queue: Queue[ConsumerRecord[K, V]]): Unit = {
        val (consumerRecord, tail) = queue.dequeue
        q = tail
        push(out, consumerRecord)
      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          q = grab(in)
          if(q != null || !q.isEmpty) doQ(q)
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          if(q == null || q.isEmpty) pull(in) else doQ(q)
        }
      })
    }
  }
}
