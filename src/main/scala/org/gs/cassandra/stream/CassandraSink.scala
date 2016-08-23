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
import akka.stream.{ActorAttributes, Attributes, Inlet, SinkShape, Supervision}
import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.scaladsl.Sink
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler}
import com.datastax.driver.core.{BoundStatement, Session}
import com.datastax.driver.core.exceptions.{NoHostAvailableException,
  QueryExecutionException,
  QueryValidationException,
  UnavailableException,
  UnsupportedFeatureException,
  WriteTimeoutException}
import scala.util.control.NonFatal
import CassandraQuery.decider

/** Execute Cassandra BoundStatements that don't return Rows (Insert, Update, Delete). Values are
  * bound to the statement in the previous stage. BoundStatements can be for different queries.
  *
  * @param session is long lived, it's created sometime before the stream and closed sometime after
  * the stream and may be used with other clients
  * @param implicit logger
  * @author Gary Struthers
  */
class CassandraSink(session: Session)(implicit logger: LoggingAdapter)
    extends GraphStage[SinkShape[BoundStatement]] {

  val in = Inlet[BoundStatement]("CassandraSink.in")
  override val shape: SinkShape[BoundStatement] = SinkShape(in)

  /** When upstream pushes a BoundStatement execute it asynchronously and use Cassandra's preferred
    * getUniterruptibly method on ResultSetFuture. Then pull another BoundStatement
    *
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      private def decider = inheritedAttributes.get[SupervisionStrategy].map(_.decider).
          getOrElse(Supervision.stoppingDecider)

      /** start backpressure in custom Sink */
      override def preStart(): Unit = {
        pull(in)
      }

      def executeStmt(stmt: BoundStatement): Unit = {
        try {
          val resultSetFuture = session.executeAsync(stmt)
          val rs = resultSetFuture.getUninterruptibly()
          logger.debug("BoundStatement success")
          pull(in)
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
          executeStmt(boundStatement: BoundStatement)
        }
      })
    }
  }
}

object CassssandraSink {

  /** Create CassandraSink as Akka Flow with Supervision
    *
    * @param session Cassandra Session
    * @param implicit logger
    * @return Sink[BoundStatement, NotUsed]
    */
  def apply(session: Session)(implicit logger: LoggingAdapter): Sink[BoundStatement, NotUsed] = {
    val sink = Sink.fromGraph(new CassandraSink(session))
    sink.withAttributes(ActorAttributes.supervisionStrategy(decider))
  }
}
