/** Copyright 2016 Gary Struthers

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
package org.gs.kafka.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{ActorAttributes, Attributes, Outlet, SourceShape, Supervision}
import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.consumer.{CommitFailedException, InvalidOffsetException}
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.errors.{AuthorizationException, WakeupException}
import scala.util.control.NonFatal
import org.gs.kafka.stream.KafkaSource.decider

/** A mock version of [[org.gs.kafka.stream.KafkaSource]] to test KafkaSource's exception handling
  *
  * @tparam V value
  * @iter input values
  * @param testException for injecting exceptions to test Supervision
  * @author Gary Struthers
  */
class MockKafkaSource[V](iter: Iterator[V], testException: RuntimeException = null)
        (implicit logger: LoggingAdapter) extends GraphStage[SourceShape[V]]{

  val out = Outlet[V](s"KafkaSource")
  override val shape = SourceShape(out)

  /** On downstream pull check if exception injected and throw it, KafkaSink Supervision decides
    * what handler should be used. Supervision.Resume is handled by retrying the poll or commit, up
    * to the # of retries. Supervision.Stop stops the Stage
    *
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      private def decider = inheritedAttributes.get[SupervisionStrategy].map(_.decider).
          getOrElse(Supervision.stoppingDecider)

      private var retries = 3
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          try {
            if(testException != null) throw testException
            if(iter.hasNext) {
              push(out, iter.next())
            }
            retries = 3
          } catch {
            case NonFatal(e) => decider(e) match {
              case Supervision.Stop => {
                failStage(e)
              }
              case Supervision.Resume => {
                if(retries > 0) {
                  logger.debug("MockKafkaSource Resume exception retries:{}", retries)
                  retries = retries - 1
                  onPull
                } else {
                  failStage(e) // too many retries
                }
              }
            }
          }
        }
      })
    }
  }
}

/** Create a Mock Kafka Source with KafkaSource Supervision */
object MockKafkaSource {

  /** Create Kafka Sink as Akka Sink subscribed to configured topic with Supervision
    * @tparam V value type
    * @param iter test values
    * @param testException injected exception, can be null
  	* @param implicit logger
  	* @return Source[V, NotUsed]
  	*/
  def apply[V](iter: Iterator[V], testException: RuntimeException)(implicit logger: LoggingAdapter):
        Source[V, NotUsed] = {
    val source = Source.fromGraph(new MockKafkaSource[V](iter, testException))
    source.withAttributes(ActorAttributes.supervisionStrategy(decider))
  }
}
