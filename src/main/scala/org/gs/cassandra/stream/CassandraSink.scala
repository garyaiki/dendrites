package org.gs.cassandra.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{Attributes, Inlet, SinkShape}
import akka.stream.scaladsl.Sink
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler}
import com.datastax.driver.core.{BoundStatement, Session}

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
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/UnsupportedFeatureException.html UnsupportedFeatureException]]
    * @throws UnsupportedFeatureException - if the protocol version 1 is in use and a feature not
    * supported has been used. Features that are not supported by the version protocol 1 include:
    * BatchStatement, ResultSet paging and binary values in RegularStatement.
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/NoHostAvailableException.html NoHostAvailableException]]
    * @throws NoHostAvailableException - if no host in the cluster can be contacted successfully.
    * @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/QueryExecutionException.html QueryExecutionException]]
		* @throws QueryExecutionException - if the query triggered an execution exception, that is an
		* exception thrown by Cassandra when it cannot execute the query with the requested consistency
		* level successfully.
		* @see [[http://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/exceptions/QueryValidationException.html QueryValidationException]]
		* @throws QueryValidationException - if the query is invalid (syntax error, unauthorized or any
		* other validation problem).
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      /** start backpressure in custom Sink */
      override def preStart(): Unit = {
        pull(in)
      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val boundStatement = grab(in)
          val rsF = session.executeAsync(boundStatement)
          val rs = rsF.getUninterruptibly()
          logger.debug("BoundStatement success")
          pull(in)
        }
      })
    }
  }
}

object CassssandraSink {
  def apply(session: Session)(implicit logger: LoggingAdapter): Sink[BoundStatement, NotUsed] = {
    Sink.fromGraph(new CassandraSink(session))
  }
}
