/** Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.github.garyaiki.dendrites.cassandra.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{Attributes, Inlet, SinkShape}
import akka.stream.scaladsl.Sink
import akka.stream.stage.{AsyncCallback, GraphStage, GraphStageLogic, InHandler, TimerGraphStageLogic}
import com.datastax.driver.core.{ResultSet, ResultSetFuture, Session}
import com.datastax.driver.core.exceptions.DriverException
import com.google.common.util.concurrent.ListenableFuture
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.util.control.NonFatal
import com.github.garyaiki.dendrites.cassandra.getFailedColumnNames
import com.github.garyaiki.dendrites.concurrent.listenableFutureToScala
import com.github.garyaiki.dendrites.stream.TimerConfig

/** Execute Cassandra BoundStatements that don't return Rows (Insert, Update, Delete). Values are
  * bound to the statement in the previous stage. BoundStatements can be for different queries.
  *
  * Cassandra's Async Execute statement returns a Guava ListenableFuture which is converted to a
  * completed Scala Future.
  * Success invokes an Akka Stream AsyncCallback which pulls
  * Failure invokes an Akka Stream AsyncCallback which fails the stage
  *
  * Cassandra's Java driver handles retry and reconnection, so Supervision isn't used
  *
  * @tparam A input type
  * @param tc: TimerConfig
  * @param f map case class to ResultSet, Usually curried function
  * @param logger implicit LoggingAdapter
  * @author Gary Struthers
  */
class CassandraRetrySink[A <: Product](tc: TimerConfig, f: (A) => ResultSetFuture)(implicit val ec: ExecutionContext,
  logger: LoggingAdapter) extends GraphStage[SinkShape[A]] {

  val in = Inlet[A]("CassandraRetrySink.in")
  override val shape: SinkShape[A] = SinkShape(in)

  /** When upstream pushes a case class execute it asynchronously. Then pull
    *
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new TimerGraphStageLogic(shape) {

      var retries = 0
      val maxDuration = tc.maxDuration
      val curriedDelay = tc.curriedDelay
      var waitForTimer: Boolean = false

      /** start backpressure in custom Sink */
      override def preStart(): Unit = pull(in)

      def myCallBack(tuple: (A, ResultSet)): Unit = {
        val caseClass = tuple._1
        val rs = tuple._2
        if (!rs.wasApplied) {
          val sb = getFailedColumnNames(rs.one)
          sb.append(" case class:").append(caseClass.productPrefix).append(" retries:").append(retries)
          val msg = sb.toString
          val duration = curriedDelay(retries)
          if (duration < maxDuration) {
            waitForTimer = true
            logger.warning(msg)
            scheduleOnce(None, duration)
          } else {
            failStage(new DriverException(msg))
          }
        } else {
          retries = 0
          pull(in)
        }
      }

      def myHandler(caseClass: A): Unit = {
        try {
          val resultSetFuture = f(caseClass)
          val scalaRSF = listenableFutureToScala[ResultSet](resultSetFuture.asInstanceOf[ListenableFuture[ResultSet]])
          scalaRSF.onComplete {
            case Success(rs) => {
              val successCallback = getAsyncCallback { myCallBack }
              successCallback.invoke((caseClass, rs))
            }
            case Failure(t) => {
              val failCallback = getAsyncCallback {
                (_: Unit) => {
                  logger.error(t, "CassandraSink ListenableFuture fail e:{}", t.getMessage)
                  failStage(t)
                }
              }
              failCallback.invoke(t)
            }
          }
        } catch { case e: Throwable => failStage(e) }

      }

      override protected def onTimer(timerKey: Any): Unit = {
        retries += 1
        waitForTimer = false
        timerKey match {
          case caseClass: A => myHandler(caseClass)
          case x => throw new IllegalArgumentException(s"expected caseClass received:${x.toString}")
        }

      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          if (!waitForTimer) {
            val caseClass = grab(in)
            myHandler(caseClass)
          }
        }
      })
    }
  }
}

object CassssandraRetrySink {

  /** Create CassandraSink as Akka Sink
    *
    * @tparam A input type
    * @param tc: TimerConfig
    * @param f map case class to ResultSetFuture, Usually curried function
    * @return Sink[A, NotUsed]
    */
  def apply[A <: Product](tc: TimerConfig, f: (A) => ResultSetFuture)(implicit ec: ExecutionContext,
    logger: LoggingAdapter): Sink[A, NotUsed] = {
    Sink.fromGraph(new CassandraRetrySink[A](tc, f))
  }
}
