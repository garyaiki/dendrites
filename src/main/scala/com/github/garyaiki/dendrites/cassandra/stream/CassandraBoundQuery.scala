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
import akka.stream.stage.{AsyncCallback, GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.datastax.driver.core.{BoundStatement, PreparedStatement, ResultSet, Session}
import com.datastax.driver.core.exceptions.NoHostAvailableException
import com.google.common.util.concurrent.ListenableFuture
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.util.control.NonFatal
import com.github.garyaiki.dendrites.cassandra.{getRowColumnNames, noHostAvailableExceptionMsg, sessionLogInfo}
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
  * @param stmt: PreparedStatement
  * @param f function to map PreparedStatement to BoundStatement from with case class values
  * @param fetchSize used by SELECT queries for page size. Default 0 means use Cassandra default
  * @param ec implicit ExecutionContext
  * @param logger implicit LoggingAdapter
  *
  * @author Gary Struthers
  */
class CassandraBoundQuery[A](session: Session, stmt: PreparedStatement, f:(PreparedStatement, A) => BoundStatement,
  fetchSize: Int = 0)(implicit val ec: ExecutionContext, logger: LoggingAdapter)
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

      var waitForHandler: Boolean = false
      var mustFinish: Boolean = false

      def executeStmt(stmt: BoundStatement): Unit = {
        val resultSetFuture = session.executeAsync(stmt)
        val scalaRSF = listenableFutureToScala[ResultSet](resultSetFuture.asInstanceOf[ListenableFuture[ResultSet]])
        scalaRSF.onComplete {
          case Success(rs) => {
            val successCallback = getAsyncCallback {
              (_: Unit) => {
                logger.debug("CassandraBoundQuery Success available:{} fully fetched:{}",
                  rs.getAvailableWithoutFetching, rs.isFullyFetched)
                push(out, rs)
                waitForHandler = false
                if(mustFinish) completeStage()
              }
            }
            successCallback.invoke(rs)
          }
          case Failure(t) => {
              val failCallback = getAsyncCallback {
                (_: Unit) => {
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

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val id: A = grab(in)
          waitForHandler = true
          val msg = sessionLogInfo(session)
          logger.debug("CassandraBoundQuery onPush " + msg)
          val boundStatement = f(stmt, id)
          boundStatement.setFetchSize(fetchSize)
          executeStmt(boundStatement)
        }

      override def onUpstreamFinish(): Unit = {
          if(!waitForHandler) {
            super.onUpstreamFinish()
          } else {
            logger.debug(s"CassandraBoundQuery received onUpstreamFinish waitForHandler:$waitForHandler")
            mustFinish = true
          }
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = pull(in)
      })
    }
  }
}

object CassandraBoundQuery {

  /** Create CassandraBoundQuery as Flow
    *
    * @tparam A input type
    * @param session Cassandra Session
    * @param stmt PreparedStatement
    * @param f function to map PreparedStatement to BoundStatement with values to bind
    * @param fetchSize limits how many results retrieved simultaneously, 0 means use default size
    * @return Flow[A, ResultSet, NotUsed]
    */
  def apply[A](session: Session, stmt: PreparedStatement, f:(PreparedStatement, A) => BoundStatement, fetchNum: Int = 0)
    (implicit ec: ExecutionContext, logger: LoggingAdapter): Flow[A, ResultSet, NotUsed] = {
    Flow.fromGraph(new CassandraBoundQuery[A](session, stmt, f, fetchNum))
  }
}
