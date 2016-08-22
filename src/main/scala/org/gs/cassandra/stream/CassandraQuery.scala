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
import akka.stream.{ActorAttributes, Attributes, FlowShape, Inlet, Outlet, Supervision}
import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.datastax.driver.core.{BoundStatement, ResultSet, Session}
import com.datastax.driver.core.exceptions.{NoHostAvailableException,
  QueryExecutionException,
  QueryValidationException,
  ReadTimeoutException,
  UnavailableException,
  UnsupportedFeatureException,
  WriteTimeoutException}
import scala.util.control.NonFatal

/** Execute Cassandra BoundStatement queries that return Rows. Values are
  * bound to the statement in the previous stage. BoundStatements can be for different queries.
  *
  * @param session is long lived, it's created sometime before the stream and closed sometime after
  * the stream and may be used with other clients
  * @param fetchSize used by SELECT queries for page size. Default 0 means use Cassandra default
  * @param implicit logger
  * @author Gary Struthers
  */
class CassandraQuery(session: Session, fetchSize: Int = 0)(implicit logger: LoggingAdapter)
    extends GraphStage[FlowShape[BoundStatement, ResultSet]]{

  val in = Inlet[BoundStatement]("CassandraQuery.in")
  val out = Outlet[ResultSet]("CassandraQuery.out")
  override val shape = FlowShape.of(in, out)

  /** When upstream pushes a BoundStatement execute it asynchronously and use Cassandra's preferred
    * getUniterruptibly method on ResultSetFuture. Then push the ResultSet
    *
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      private def decider = inheritedAttributes.get[SupervisionStrategy].map(_.decider).
          getOrElse(Supervision.stoppingDecider)

      def executeStmt(stmt: BoundStatement): Unit = {
        try {
          val resultSetFuture = session.executeAsync(stmt)
          val rs = resultSetFuture.getUninterruptibly()
          logger.debug("BoundStatement query success")
          push(out, rs)
        } catch {
          case NonFatal(e) => decider(e) match {
            case Supervision.Stop => {
              logger.error(e, "Query Stop exception")
              failStage(e)
            }
            case Supervision.Resume => {
              e match {
                case e: UnavailableException => {
                  logger.debug(s"""UnavailableException address:${e.getAddress()} aliveReplicas:
                    ${e.getAliveReplicas()} consistency level:${e.getConsistencyLevel()}
                    required replicas:${e.getRequiredReplicas()}""")
                }
                case e: ReadTimeoutException => {
                  logger.debug(s"""ReadTimeoutException:${e.getMessage} wasDataRetrieved:
                    ${e.wasDataRetrieved()}""")
                }
                case e: WriteTimeoutException => {
                  logger.debug(s"WriteTimeoutException:${e.getMessage} writeType:${e.getWriteType}")
                }
                case _ => logger.debug("Query Retryable exception :{}", e.getMessage)
              }
              executeStmt(stmt)
            }
          }
        }        
      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val boundStatement = grab(in)
          boundStatement.setFetchSize(fetchSize)
          executeStmt(boundStatement: BoundStatement)
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

  /** Create CassandraQuery as Flow with Supervision
    *
    * @param session Cassandra Session
    * @param fetchSize limits how many results retrieved simultaneously, 0 means use default size
    * @param implicit logger
    * @return Flow[BoundStatement, ResultSet, NotUsed]
    */
  def apply(session: Session, fetchSize: Int = 0)
          (implicit logger: LoggingAdapter): Flow[BoundStatement, ResultSet, NotUsed] = {
    val query = Flow.fromGraph(new CassandraQuery(session, fetchSize))
    query.withAttributes(ActorAttributes.supervisionStrategy(decider))
  }

  /** Supervision strategy
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/UnavailableException.html UnavailableException]]
   	* @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/UnsupportedFeatureException.html UnsupportedFeatureException]]
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/NoHostAvailableException.html NoHostAvailableException]]
		* @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/ReadTimeoutException.html ReadTimeoutException]]
		* @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/WriteTimeoutException.html WriteTimeoutException]]
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/QueryExecutionException.html QueryExecutionException]]
		* @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/QueryValidationException.html QueryValidationException]]
    */
  def decider: Supervision.Decider = {
    case _: UnavailableException => Supervision.Resume
    case _: ReadTimeoutException => Supervision.Resume
    case _: QueryExecutionException => Supervision.Resume
    case _: NoHostAvailableException => Supervision.Stop
    case _: QueryValidationException => Supervision.Stop
    case _: UnsupportedFeatureException => Supervision.Stop
  }
}
