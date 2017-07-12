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
package com.github.garyaiki.dendrites.cassandra.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.scaladsl.Flow
import akka.stream.stage.{AsyncCallback, GraphStage, GraphStageLogic, InHandler, OutHandler, TimerGraphStageLogic}
import com.datastax.driver.core.{ResultSet, ResultSetFuture, Session}
import com.datastax.driver.core.exceptions.{DriverException, NoHostAvailableException}
import com.google.common.util.concurrent.ListenableFuture
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import scala.util.control.NonFatal
import com.github.garyaiki.dendrites.cassandra.{getRowColumnNames, noHostAvailableExceptionMsg}
import com.github.garyaiki.dendrites.concurrent.listenableFutureToScala
import com.github.garyaiki.dendrites.kafka.ConsumerRecordMetadata
import com.github.garyaiki.dendrites.stream.TimerConfig

/** Input ConsumerRecordMetadata containing a key and a  value, a function executes BoundStatement(s) on value, pass
  * input to next stage. Then an event logger can use the key as an event id with the value and optionally timestamp
  *
  * Cassandra's executeAsync statement returns a Guava ListenableFuture which is converted to a completed Scala Future.
  * Future's Success invokes an Akka Stream AsyncCallback. This checks if the conditional statement succeeded. If so,
  * push input to next stage. If statement failed, log, then schedule a retry using TimerConfig and exponential backoff
  * until max duration exceeded. On retry failure, log, then fail stage.
  *
  * Future's Failure invokes an Akka Stream AsyncCallback which fails the stage
  *
  * Cassandra exceptions that may be temporary are handled by the Java driver. It reconnects and retries the statement
  * Akka Supervision isn't used.
  *
  * If upstream completes while busy, wait for handler to complete, then completeStage
  *
  * @tparam K key type
  * @tparam V value type
  * @param tc: TimerConfig
  * @param f map value case class to ResultSet, Usually this function is curried with only input data argument passed at
  * this stage, arguments that don't change for the life of the stream are passed at stream creation.
  * @param ec implicit ExecutionContextExecutor
  * @param logger implicit LoggingAdapter
  * @author Gary Struthers
  */
class CassandraKeyValueFlow[K, V <: Product](tc: TimerConfig, f: (V) => ResultSetFuture)
  (implicit val ec: ExecutionContextExecutor, logger: LoggingAdapter) extends
    GraphStage[FlowShape[(ConsumerRecordMetadata[K], V), (ConsumerRecordMetadata[K], V)]] {

  val in = Inlet[(ConsumerRecordMetadata[K], V)]("CassandraKeyValueFlow.in")
  val out = Outlet[(ConsumerRecordMetadata[K], V)]("CassandraKeyValueFlow.out")
  override val shape: FlowShape[(ConsumerRecordMetadata[K], V), (ConsumerRecordMetadata[K], V)] = FlowShape.of(in, out)

  /** When upstream pushes a case class execute it asynchronously. Then push the input
    *
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new TimerGraphStageLogic(shape) {

      var retries = 0
      val maxDuration = tc.maxDuration
      val curriedDelay = tc.curriedDelay
      var waitForTimer: Boolean = false
      var waitForHandler: Boolean = false
      var mustFinish: Boolean = false

      def myCallBack(kvRs: ((ConsumerRecordMetadata[K], V), ResultSet)): Unit = {
        val kv = kvRs._1
        val rs = kvRs._2
        val caseClass = kv._2
        if (!rs.wasApplied) {
          val sb = getRowColumnNames(rs.one)
          sb.append(" myCallBack fail case class:").append(caseClass.productPrefix).append(" retries:").append(retries)
          val duration = curriedDelay(retries)
          if (duration < maxDuration) {
            waitForTimer = true
            logger.info(sb.toString)
            scheduleOnce(kv, duration)
          } else {
            sb.append(" duration:").append(duration).append(" maxDuration:").append(maxDuration)
            val msg = sb.toString
            logger.error(msg)
            failStage(new DriverException(msg))
          }
        } else {
          retries = 0
          push(out, kv)
          waitForHandler = false
          if(mustFinish) { completeStage() }
        }
      }

      def myHandler(kv: (ConsumerRecordMetadata[K], V)): Unit = {
        waitForHandler = true
        val resultSetFuture = f(kv._2)
        val scalaRSF = listenableFutureToScala[ResultSet](resultSetFuture.asInstanceOf[ListenableFuture[ResultSet]])
        scalaRSF.onComplete {
          case Success(rs) => {
            val successCallback = getAsyncCallback { myCallBack }
            successCallback.invoke((kv, rs))
          }
          case Failure(t) => {
            val failCallback = getAsyncCallback {
              (_: Unit) =>
                {
                  t match {
                    case e: NoHostAvailableException => {
                      val msg = noHostAvailableExceptionMsg(e)
                      logger.error(t, "myHandler NoHostAvailableException e:{}", msg)
                    }
                    case _ => logger.error(t, "myHandler ListenableFuture fail e:{}", t.getMessage)
                  }
                  failStage(t)
                }
            }
            failCallback.invoke(t)
          }
        }
      }

      override protected def onTimer(timerKey: Any): Unit = {
        retries += 1
        waitForTimer = false
        timerKey match {
          case kv: (ConsumerRecordMetadata[K], V)  => myHandler(kv)
          case x: Any => throw new IllegalArgumentException(s"onTimer expected (K, V) received:${x.toString}")
        }
      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          if (!waitForTimer) {
            val kv = grab(in)
            myHandler(kv)
          }
        }
        override def onUpstreamFinish(): Unit = {
          if(!waitForHandler) { super.onUpstreamFinish() } else { mustFinish = true }
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = pull(in)
      })
    }
  }
}

object CassandraKeyValueFlow {

  /** Create CassandraKeyValueFlow
    *
    * @tparam K key type
    * @tparam V value type case class or tuple
    * @param tc: TimerConfig
    * @param f map case class to ResultSetFuture, Usually a curried function
    * @return Flow[(ConsumerRecordMetadata[K], V), (ConsumerRecordMetadata[K], V), NotUsed]
    */
  def apply[K, V <: Product](tc: TimerConfig, f: (V) => ResultSetFuture)
    (implicit ec: ExecutionContextExecutor, logger: LoggingAdapter):
      Flow[(ConsumerRecordMetadata[K], V), (ConsumerRecordMetadata[K], V), NotUsed] = {

      Flow.fromGraph(new CassandraKeyValueFlow[K, V](tc, f))
    }
}
