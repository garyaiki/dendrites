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

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords}
import scala.collection.immutable.Queue

/** Flow that takes a Queue of ConsumerRecord and deques one on pull
  *
  * Flow that uses a queue to send messages one at a time. A Queue[ConsumerRecord] is pushed
  * from upstream, when downstream pulls, 1 ConsumerRecord is dequeued and pushed downstream
  *
  * @tparam K ConsumerRecord key
  * @tparam V ConsumerRecord value
  * @param f function maps ConsumerRecords to Queue[ConsumerRecord[K, V]]
  * @author Gary Struthers
  */
class ConsumerRecordsToQueue[K, V](f:(ConsumerRecords[K,V]) => Queue[ConsumerRecord[K, V]]) extends
  GraphStage[FlowShape[ConsumerRecords[K, V], ConsumerRecord[K, V]]] {

  val in = Inlet[ConsumerRecords[K, V]] ("ConsumerRecords.in")
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
          val i = grab(in)
          q = f(i)
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
