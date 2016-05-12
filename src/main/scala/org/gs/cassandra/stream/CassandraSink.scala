package org.gs.cassandra.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{ Attributes, Inlet, SinkShape }
import akka.stream.scaladsl.Sink
import akka.stream.stage.{ GraphStage, GraphStageLogic, InHandler }
import com.datastax.driver.core.{BoundStatement, Session}

/** Execute Cassandra BoundStatements that don't return Rows (Insert, Update, Delete). Values are
  * bound to the statement in the previous stage. BoundStatements can be for different queries.
  *
  * @author Gary Struthers
  * @session is long lived, it's created sometime before the stream and closed sometime after the
  * stream and may be used with other clients
  * @implicit logger
  */
class CassandraSink(session: Session)(implicit logger: LoggingAdapter)
    extends GraphStage[SinkShape[BoundStatement]] {

  val in = Inlet[BoundStatement]("CassandraSink.in")
  override val shape: SinkShape[BoundStatement] = SinkShape(in)

  /** When upstream pushes a BoundStatement execute it asychronously and use Cassandra's preferred
    * getUniterruptibly method on ResultSetFuture. Then pull another BoundStatement
    * 
    * @param inheritedAttributes
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
