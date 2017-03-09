/** Copyright 2016 - 2017 Gary Struthers

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
import akka.stream.stage.{AsyncCallback, GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.datastax.driver.core.{BoundStatement, ResultSet, Row, Session}
import com.google.common.util.concurrent.ListenableFuture
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.util.control.NonFatal
import com.github.garyaiki.dendrites.concurrent.listenableFutureToScala

/** Execute Cassandra conditional BoundStatement insert, update, delete that return 1 Row. The conditional statement
  * returns a ResultSet with one row. It contains a generated boolean column named "[applied]". It's true on success, on
  * failure it's false and all columns of the statement are returned.
  *
  * An Option[Row] is passed to the next stage, which should be a custom error handler. It's an Option[Row] to handle
  * unconditional insert, update, delete. Those return an empty ResultSet.
  *
  * Do not use this with Queries, they usually return more than one row and they would be lost, use CassandraQuery.
  * Unconditional insert, update, and delete should use CassandraSink. Conditional delete has a performance penalty and
  * unconditional delete already checks If Exists
  * Values bound in previous stage BoundStatements can be for different queries.
  *
  * Cassandra's Async Execute statement returns a Guava ListenableFuture which is converted to a
  * completed Scala Future.
  * Success invokes an Akka Stream AsyncCallback which pushes the ResultSet
  * Failure invokes an Akka Stream AsyncCallback which fails the stage
  *
  * Cassandra's Java driver handles retry and reconnection, so Supervision isn't used
  *
  * @param session created and closed elsewhere
  * @param rowFun: ResultSet => Option[Row] handle row returned by conditional stmt
  * @param ec implicit ExecutionContext
  * @param logger implicit LoggingAdapter
  *
  * @author Gary Struthers
  */
class CassandraConditional(session: Session, rowFun: ResultSet => Option[Row])(implicit val ec: ExecutionContext,
    logger: LoggingAdapter) extends GraphStage[FlowShape[BoundStatement, Option[Row]]] {

  val in = Inlet[BoundStatement]("CassandraConditional.in")
  val out = Outlet[Option[Row]]("CassandraConditional.out")
  override val shape = FlowShape.of(in, out)

  /** When upstream pushes a BoundStatement execute it asynchronously. Then push the ResultSet
    *
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      def executeStmt(stmt: BoundStatement): Unit = {
        val resultSetFuture = session.executeAsync(stmt)
        val scalaRSF = listenableFutureToScala[ResultSet](resultSetFuture.asInstanceOf[ListenableFuture[ResultSet]])
        scalaRSF.onComplete {
          case Success(rs) => {
            val successCallback = getAsyncCallback{
              (rs: ResultSet) => push(out, rowFun(rs))
            }
            successCallback.invoke(rs)
          }
          case Failure(t) => {
            val failCallback = getAsyncCallback{
              (_: Unit) => {
                logger.error(t, "CassandraQuery ListenableFuture fail e:{}", t.getMessage)
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
          executeStmt(boundStatement)
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = pull(in)
      })
    }
  }
}

object CassandraConditional {

  /** Create CassandraConditional as Flow
    *
    * @param session Cassandra Session
    * @param rowFun: ResultSet => Option[Row] handle row returned by conditional stmt
    * @param ec implicit ExecutionContext
    * @param logger implicit LoggingAdapter
    * @return Flow[BoundStatement, Option[Row], NotUsed]
    */
  def apply(session: Session, rowFun: ResultSet => Option[Row])(implicit ec: ExecutionContext, logger: LoggingAdapter):
    Flow[BoundStatement, Option[Row], NotUsed] = {
    Flow.fromGraph(new CassandraConditional(session, rowFun))
  }
}
