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
package org.gs.cassandra.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.scaladsl.Flow
import akka.stream.stage.{AsyncCallback, GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.datastax.driver.core.{BoundStatement, ResultSet, Session}
import com.google.common.util.concurrent.ListenableFuture
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.util.control.NonFatal
import org.gs.concurrent.listenableFutureToScala

/** Execute Cassandra BoundStatement queries that return Rows. Values bound in previous stage.
  * BoundStatements can be for different queries.
  *
  * Cassandra's Async Execute statement returns a Guava ListenableFuture which is converted to a
  * completed Scala Future.
  * Success invokes an Akka Stream AsyncCallback which pushes the ResultSet
  * Failure invokes an Akka Stream AsyncCallback which fails the stage
  *
  * Cassandra's Java driver handles retry and reconnection, so Supervision isn't used
  *
  * @param session created and closed elsewhere
  * @param fetchSize used by SELECT queries for page size. Default 0 means use Cassandra default
  * @param ec implicit ExecutionContext
  * @param logger implicit LoggingAdapter
  *
  * @author Gary Struthers
  */
class CassandraQuery(session: Session, fetchSize: Int = 0)(implicit val ec: ExecutionContext,
            logger: LoggingAdapter) extends GraphStage[FlowShape[BoundStatement, ResultSet]]{

  val in = Inlet[BoundStatement]("CassandraQuery.in")
  val out = Outlet[ResultSet]("CassandraQuery.out")
  override val shape = FlowShape.of(in, out)

  /** When upstream pushes a BoundStatement execute it asynchronously. Then push the ResultSet
    *
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      def executeStmt(stmt: BoundStatement): Unit = {
        val resultSetFuture = session.executeAsync(stmt)
        val scalaRSF = listenableFutureToScala[ResultSet](
                resultSetFuture.asInstanceOf[ListenableFuture[ResultSet]])
        scalaRSF.onComplete {
          case Success(rs) => {
            val successCallback = getAsyncCallback{ (_: Unit) => push(out, rs) }
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
          boundStatement.setFetchSize(fetchSize)
          executeStmt(boundStatement)
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
  }
}

object CassandraQuery {

  /** Create CassandraQuery as Flow
    *
    * @param session Cassandra Session
    * @param fetchSize limits how many results retrieved simultaneously, 0 means use default size
    * @return Flow[BoundStatement, ResultSet, NotUsed]
    */
  def apply(session: Session, fetchSize: Int = 0)(implicit ec: ExecutionContext,
              logger: LoggingAdapter): Flow[BoundStatement, ResultSet, NotUsed] = {
    Flow.fromGraph(new CassandraQuery(session, fetchSize))
  }
}
