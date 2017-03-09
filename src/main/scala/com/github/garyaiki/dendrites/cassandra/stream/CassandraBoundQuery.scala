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
import com.datastax.driver.core.{BoundStatement, PreparedStatement, ResultSet, Session}
import com.google.common.util.concurrent.ListenableFuture
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.util.control.NonFatal
import com.github.garyaiki.dendrites.concurrent.listenableFutureToScala

/** Map case class to BoundStatement query, execute it, Same as CassandraBind ~> CassandraQuery in 1 stage
  *
  * Cassandra's Async Execute statement returns a Guava ListenableFuture which is converted to a
  * completed Scala Future.
  * Success invokes an Akka Stream AsyncCallback which pushes the ResultSet
  * Failure invokes an Akka Stream AsyncCallback which fails the stage
  *
  * Cassandra's Java driver handles retry and reconnection, so Supervision isn't used
  *
  * @tparam A input type
  * @param session: Session
  * @param fetchSize used by SELECT queries for page size. Default 0 means use Cassandra default
  * @param stmt: PreparedStatement
  * @param f function to map PreparedStatement to BoundStatement from with case class values
  * @param ec implicit ExecutionContext
  * @param logger implicit LoggingAdapter
  *
  * @author Gary Struthers
  */
class CassandraBoundQuery[A](session: Session, fetchSize: Int = 0, stmt: PreparedStatement,
  f:(PreparedStatement, A) => BoundStatement)(implicit val ec: ExecutionContext, logger: LoggingAdapter)
  extends GraphStage[FlowShape[A, ResultSet]] {

  val in = Inlet[A]("CassandraBoundQuery.in")
  val out = Outlet[ResultSet]("CassandraQuery.out")
  override val shape = FlowShape.of(in, out)

  /** When upstream pushes a case class, create a BoundStatement execute it asynchronously. Then push the ResultSet
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
            val successCallback = getAsyncCallback{ (_: Unit) => push(out, rs) }
            successCallback.invoke(rs)
          }
          case Failure(t) => {
            val failCallback = getAsyncCallback{
              (_: Unit) => {
                logger.error(t, "CassandraBoundQuery ListenableFuture fail e:{}", t.getMessage)
                failStage(t)
              }
            }
            failCallback.invoke(t)
          }
        }
      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val id: A = grab(in)
          val boundStatement = f(stmt, id)
          boundStatement.setFetchSize(fetchSize)
          executeStmt(boundStatement)
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = pull(in)
      })
    }
  }
}

object CassandraBoundQuery {

  /** Create CassandraQuery as Flow
    *
    * @tparam A input type
    * @param session Cassandra Session
    * @param fetchSize limits how many results retrieved simultaneously, 0 means use default size
    * @param stmt PreparedStatement
    * @param f function to map PreparedStatement to BoundStatement with values to bind
    * @return Flow[A, ResultSet, NotUsed]
    */
  def apply[A](session: Session, fetchNum: Int = 0, stmt: PreparedStatement, f:(PreparedStatement, A) => BoundStatement)
    (implicit ec: ExecutionContext, logger: LoggingAdapter): Flow[A, ResultSet, NotUsed] = {
    Flow.fromGraph(new CassandraBoundQuery[A](session, fetchNum, stmt, f))
  }
}
