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
import com.datastax.driver.core.exceptions.{DriverException, NoHostAvailableException}
import com.google.common.util.concurrent.ListenableFuture
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.util.control.NonFatal
import com.github.garyaiki.dendrites.cassandra.{getRowColumnNames, noHostAvailableExceptionMsg}
import com.github.garyaiki.dendrites.concurrent.listenableFutureToScala
import com.github.garyaiki.dendrites.stream.TimerConfig

/** Input case class, a higher order function asynchronously executes BoundStatement(s) on value.
  *
  * Cassandra's executeAsync statement returns a Guava ListenableFuture which is converted to a completed Scala Future.
  * Future's Success invokes an Akka Stream AsyncCallback. This checks if the conditional statement succeeded. If
  * statement failed, log, then schedule a retry using TimerConfig and exponential backoff until max duration exceeded.
  * On retry failure, log, then fail stage.
  *
  * Future's Failure invokes an Akka Stream AsyncCallback which fails the stage
  *
  * Cassandra exceptions that may be temporary are handled by the Java driver. It reconnects and retries the statement
  * Akka Supervision isn't used.
  *
  * If upstream completes while busy, wait for handler to complete, then completeStage
  *
  * @tparam A input case class type
  * @param tc: TimerConfig
  * @param f map value case class to ResultSet, Usually this function is curried with only input data argument passed at
  * this stage, arguments that don't change for the life of the stream are passed at stream creation.
  * @param ec implicit ExecutionContext
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
      var waitForHandler: Boolean = false
      var mustFinish: Boolean = false

      override def preStart(): Unit = pull(in) // init stream backpressure

      def myCallBack(tuple: (A, ResultSet)): Unit = {
        val caseClass = tuple._1
        val rs = tuple._2
        if (!rs.wasApplied) {
          val sb = getRowColumnNames(rs.one)
          sb.append(" myCallBack fail case class:").append(caseClass.productPrefix).append(" retries:").append(retries)
          val duration = curriedDelay(retries)
          if (duration < maxDuration) {
            waitForTimer = true
            logger.info(sb.toString)
            scheduleOnce(None, duration)
          } else {
            sb.append(" duration:").append(duration).append(" maxDuration:").append(maxDuration)
            val msg = sb.toString
            logger.error(msg)
            failStage(new DriverException(msg))
          }
        } else {
          retries = 0
          if(mustFinish) {
            completeStage()
          } else {
            waitForHandler = false
            pull(in)
          }
        }
      }

      def myHandler(caseClass: A): Unit = {
        waitForHandler = true
        val resultSetFuture = f(caseClass)
        val scalaRSF = listenableFutureToScala[ResultSet](resultSetFuture.asInstanceOf[ListenableFuture[ResultSet]])
        scalaRSF.onComplete {
          case Success(rs) => {
            val successCallback = getAsyncCallback { myCallBack }
            successCallback.invoke((caseClass, rs))
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
          case caseClass: A => myHandler(caseClass)
          case x => throw new IllegalArgumentException(s"onTimer expected caseClass received:${x.toString}")
        }
      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          if (!waitForTimer) {
            val caseClass = grab(in)
            myHandler(caseClass)
          }
        }

        override def onUpstreamFinish(): Unit = {
          if(!waitForHandler) {
            super.onUpstreamFinish()
          } else {
            logger.debug(s"received onUpstreamFinish waitForHandler:$waitForHandler")
            mustFinish = true
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
