package org.gs.cassandra.stream

import akka.event.LoggingAdapter
import akka.stream.{Attributes, FlowShape, Inlet, Outlet }
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.datastax.driver.core.{BoundStatement, ResultSet, Session}

/** Execute Cassandra BoundStatement queries that return Rows. Values are
  * bound to the statement in the previous stage. BoundStatements can be for different queries.
  *
  * @author Gary Struthers
  * @param session is long lived, it's created sometime before the stream and closed sometime after
  * the stream and may be used with other clients
  * @param fetchSize used by SELECT queries for page size. Default 0 means use Cassandra default
  * @param implicit logger
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
  
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val boundStatement = grab(in)
          boundStatement.setFetchSize(fetchSize)
          val resultSetFuture = session.executeAsync(boundStatement)
          val rs = resultSetFuture.getUninterruptibly()
          logger.debug("BoundStatement query success")
          push(out, rs)
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
