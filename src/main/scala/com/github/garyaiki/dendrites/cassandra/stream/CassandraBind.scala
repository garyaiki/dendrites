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

import akka.event.LoggingAdapter
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.datastax.driver.core.{BoundStatement, PreparedStatement}

/** Create BoundStatement from a PreparedStatement and a Product (superclass of case class
  * and tuple)
  *
  * @tparam A input type
  * @param stmt PreparedStatement that has been pre-parsed by the database
  * @param f function to map BoundStatement from PreparedStatement with values to bind
  * @param logger implicit LoggingAdapter
  * @author Gary Struthers
  */
class CassandraBind[A](stmt: PreparedStatement, f:(PreparedStatement, A) => BoundStatement)
    (implicit logger: LoggingAdapter) extends GraphStage[FlowShape[A, BoundStatement]]{

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
