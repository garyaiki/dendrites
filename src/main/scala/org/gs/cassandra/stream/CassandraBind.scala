package org.gs.cassandra.stream

import akka.event.LoggingAdapter
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.datastax.driver.core.{BoundStatement, PreparedStatement}

/** Create BoundStatement from a PreparedStatement and a Product (superclass of case class
  * and tuple)
  *
  * @param stmt PreparedStatement that has been pre-parsed by the database
  * @param f function to create BoundStatement from PreparedStatement and Product which contains
  * values to bind
  * @param implicit logger
  * @author Gary Struthers
  */
class CassandraBind[A](stmt: PreparedStatement, f:(PreparedStatement, A) => BoundStatement)
        (implicit logger: LoggingAdapter)
    extends GraphStage[FlowShape[A, BoundStatement]]{

  val in = Inlet[A]("CassandraBind.in")
  val out = Outlet[BoundStatement]("CassandraBind.out")
  override val shape = FlowShape.of(in, out)

  /** When a Product is pushed, function f creates a BoundStatement ready to be
    * executed downstream.
    *
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          push(out, f(stmt, grab(in)))
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
