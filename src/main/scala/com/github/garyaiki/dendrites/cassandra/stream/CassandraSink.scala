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
package com.github.garyaiki.dendrites.cassandra.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{Attributes, Inlet, SinkShape}
import akka.stream.scaladsl.Sink
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler}
import com.datastax.driver.core.{BoundStatement, ResultSet, Session}
import com.google.common.util.concurrent.ListenableFuture
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.util.control.NonFatal
import com.github.garyaiki.dendrites.concurrent.listenableFutureToScala

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
  * @param session created and closed elsewhere
  * @param ec implicit ExecutionContext
  * @param logger implicit LoggingAdapter
  * @author Gary Struthers
  */
class CassandraSink(session: Session)(implicit val ec: ExecutionContext, logger: LoggingAdapter)
    extends GraphStage[SinkShape[BoundStatement]] {

  val in = Inlet[BoundStatement]("CassandraSink.in")
  override val shape: SinkShape[BoundStatement] = SinkShape(in)

  /** When upstream pushes a BoundStatement execute it asynchronously. Then pull
    *
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      /** start backpressure in custom Sink */
      override def preStart(): Unit = {
        pull(in)
      }

      def executeStmt(stmt: BoundStatement): Unit = {
        val resultSetFuture = session.executeAsync(stmt)
        val scalaRSF = listenableFutureToScala[ResultSet](resultSetFuture.asInstanceOf[ListenableFuture[ResultSet]])
        scalaRSF.onComplete {
          case Success(rs) => {
            val successCallback = getAsyncCallback{ (_: Unit) => pull(in) }
            successCallback.invoke(rs)
          }
          case Failure(t) => {
            val failCallback = getAsyncCallback{
              (_: Unit) => {
                logger.error(t, "CassandraSink ListenableFuture fail e:{}", t.getMessage)
                failStage(t)
              }
            }
            failCallback.invoke(t)
          }
        }
      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val boundStatement = grab(in)
          executeStmt(boundStatement: BoundStatement)
        }
      })
    }
  }
}

object CassssandraSink {

  /** Create CassandraSink as Akka Sink
    *
    * @param session Cassandra Session
    * @return Sink[BoundStatement, NotUsed]
    */
  def apply(session: Session)(implicit ec: ExecutionContext, logger: LoggingAdapter): Sink[BoundStatement, NotUsed] = {
    Sink.fromGraph(new CassandraSink(session))
  }
}
