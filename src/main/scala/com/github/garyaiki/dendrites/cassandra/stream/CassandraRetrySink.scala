/** Copyright 2016 - 2017 Gary Struthers
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.github.garyaiki.dendrites.cassandra.stream

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{Attributes, Inlet, SinkShape, Supervision}
import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.scaladsl.Sink
import akka.stream.stage.{AsyncCallback, GraphStage, GraphStageLogic, InHandler, TimerGraphStageLogic}
import com.datastax.driver.core.{BoundStatement, PreparedStatement, ResultSet, Session}
import com.google.common.util.concurrent.ListenableFuture
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.util.control.NonFatal
import com.github.garyaiki.dendrites.concurrent.listenableFutureToScala

/** Execute Cassandra BoundStatements that don't return Rows (Insert, Update, Delete). Values are
  * bound to the statement in the previous stage. BoundStatements can be for different queries.
  *
  * Cassandra's Async Execute statement returns a Guava ListenableFuture which is converted to a
  * completed Scala Future.
  * Success invokes an Akka Stream AsyncCallback which pulls
  * Failure invokes an Akka Stream AsyncCallback which fails the stage
  *
  * Cassandra's Java driver handles retry and reconnection, so Supervision isn't used
  *
  * @tparam A input type
  * @param session: Session
  * @param queryStmt PreparedStatement
  * @param setStmt: PreparedStatement
  * @param f map case class to ResultSet Usually curried function i.e. checkAndSetOwner
  * @param ec implicit ExecutionContext
  * @param logger implicit LoggingAdapter
  * @author Gary Struthers
  */
class CassandraRetrySink[A <: Product](session: Session, queryStmt: PreparedStatement, setStmt: PreparedStatement,
  f: (A) => ResultSet)(implicit val ec: ExecutionContext, logger: LoggingAdapter) extends GraphStage[SinkShape[A]] {

  val in = Inlet[A]("CassandraRetrySink.in")
  override val shape: SinkShape[A] = SinkShape(in)

  /** When upstream pushes a case class execute it asynchronously. Then pull
    *
    * @param inheritedAttributes
    */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new TimerGraphStageLogic(shape) {

      /** start backpressure in custom Sink */
      override def preStart(): Unit = pull(in)

      def executeStmt(caseClass: A): Unit = {
        val queryResult = f(caseClass)
      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val caseClass = grab(in)
          executeStmt(caseClass)
        }
      })
    }
  }
}

object CassssandraRetrySink {

  /** Create CassandraSink as Akka Sink
    *
    * @param session Cassandra Session
    * @return Sink[BoundStatement, NotUsed]
    */
  def apply[A <: Product](session: Session,
    queryStmt: PreparedStatement,
    setStmt: PreparedStatement,
    f: (A) => ResultSet)
    (implicit ec: ExecutionContext, logger: LoggingAdapter): Sink[A, NotUsed] = {
    Sink.fromGraph(new CassandraRetrySink[A](session, queryStmt, setStmt, f))
  }
}
