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
package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{Attributes, SinkShape, UniformFanOutShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.scaladsl.GraphDSL.Implicits._
import com.datastax.driver.core.{BoundStatement, PreparedStatement, Session}
import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps
import com.github.garyaiki.dendrites.cassandra.stream.{CassandraBind, CassandraKeyValueFlow, CassandraRetrySink,
  CassandraSink}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.avro4s.Avro4sShoppingCartCmd.toCaseClass
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.{CassandraShoppingCart,
  CassandraShoppingCartEvtLog, RetryConfig, ShoppingCartConfig}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCartEvtLog.bndInsert
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.kafka.ShoppingCartCmdConsumer
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.event.ShoppingCartEvt
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.event.cmdToEvt
import com.github.garyaiki.dendrites.kafka.ConsumerRecordMetadata
import com.github.garyaiki.dendrites.kafka.stream.avro4s.ConsumerRecordDeserializer
import com.github.garyaiki.dendrites.kafka.stream.{ConsumerRecordsToQueue, KafkaSource}
import com.github.garyaiki.dendrites.kafka.stream.extractRecords

package object stream {

  /** Kafka source polls ShoppingCart command topic, queues Kafka keys and values, deserializes Avro byte arrays to
    * ShoppingCartCmd
    *
    * @param dispatcher
    * @param ec implicit ExecutionContextExecutor
    * @param logger implicit LoggingAdapter
    * @return composite source
    */
  def shoppingCartCmdEvtSource(dispatcher: Attributes)(implicit ec: ExecutionContextExecutor, logger: LoggingAdapter):
    Source[(ConsumerRecordMetadata[String], ShoppingCartCmd), NotUsed] = {

    val kafkaSource = KafkaSource[String, Array[Byte]](ShoppingCartCmdConsumer).withAttributes(dispatcher)
    val consumerRecordQueue = new ConsumerRecordsToQueue[String, Array[Byte]](extractRecords)
    val deserializer = new ConsumerRecordDeserializer[String, ShoppingCartCmd](toCaseClass)
    kafkaSource.via(consumerRecordQueue).via(deserializer)
  }

  /** Composite Cassandra sink. CassandraKeyValueFlow executes command to store ShoppingCartCmd, then passes key value
    * input to stage that maps key/value to ShoppingCartEvt, then binds ShoppingCartEvt to a BoundStatement, then
    * inserts the event in a Cassandra event log
    *
    * @param dispatcher
    * @param session
    * @param prepStmts map of PreparedStatements used by Cassandra stages
    * @param ec implicit ExecutionContextExecutor
    * @param logger implicit Logger
    * @return composite Cassandra sink
    */
  def shoppingCartCmdEvtSink(dispatcher: Attributes, session: Session, prepStmts: Map[String, PreparedStatement])
    (implicit ec: ExecutionContextExecutor, logger: LoggingAdapter):
      Sink[(ConsumerRecordMetadata[String], ShoppingCartCmd), NotUsed] = {

    val curriedDoCmd = doShoppingCartCmd(session, prepStmts) _
    val cmdFlowGraph = new CassandraKeyValueFlow[String, ShoppingCartCmd](RetryConfig, curriedDoCmd)
      .withAttributes(dispatcher)
    val cmdFlow = Flow.fromGraph(cmdFlowGraph)
    val toEvt = Flow.fromFunction[(ConsumerRecordMetadata[String], ShoppingCartCmd), ShoppingCartEvt] {
        x => cmdToEvt(x._1, x._2)
      }
    val optInsEvtPrepStmt = prepStmts.get("InsertEvt")
    val insEvtPrepStmt = optInsEvtPrepStmt match {
      case Some(x) => x
      case None => throw new NullPointerException("ShoppingCartEvt Insert preparedStatement not found")
    }
    val bndStmt = new CassandraBind(insEvtPrepStmt, bndInsert)
    val sink = new CassandraSink(session)

    cmdFlow.via(toEvt).via(bndStmt).to(sink) // .to(Sink.ignore)
  }

  /** Composite parallel Cassandra sinks. Equivalent to shoppingCartCmdEvtSink but this splits command and event logging
    * into parallel streams. On one branch, a sink executes the command to store ShoppingCartCmd. The other branch
    * maps key/value to ShoppingCartEvt, then binds ShoppingCartEvt to a BoundStatement, then inserts the event in a
    * Cassandra event log
    *
    * @param dispatcher
    * @param session
    * @param prepStmts map of PreparedStatements used by Cassandra stages
    * @param ec implicit ExecutionContextExecutor
    * @param logger implicit Logger
    * @return composite Command sink and Event log sink
    */
  def shoppingCartCmdEvtSinks(dispatcher: Attributes, session: Session, prepStmts: Map[String, PreparedStatement])
    (implicit ec: ExecutionContextExecutor, logger: LoggingAdapter):
      Sink[(ConsumerRecordMetadata[String], ShoppingCartCmd), NotUsed] = {

    val onlyVal = Flow.fromFunction[(ConsumerRecordMetadata[String], ShoppingCartCmd), ShoppingCartCmd] { x => x._2 }
    val curriedDoCmd = doShoppingCartCmd(session, prepStmts) _
    val cmdSink = new CassandraRetrySink[ShoppingCartCmd](RetryConfig, curriedDoCmd).withAttributes(dispatcher)

    val toEvt = Flow.fromFunction[(ConsumerRecordMetadata[String], ShoppingCartCmd), ShoppingCartEvt] {
        x => cmdToEvt(x._1, x._2)
      }
    val optInsEvtPrepStmt = prepStmts.get("InsertEvt")
    val insEvtPrepStmt = optInsEvtPrepStmt match {
      case Some(x) => x
      case None => throw new NullPointerException("ShoppingCartEvt Insert preparedStatement not found")
    }
    val bndStmt = new CassandraBind(insEvtPrepStmt, bndInsert)
    val sink = new CassandraSink(session)

    Sink.fromGraph(GraphDSL.create() { implicit builder =>
      val bcast: UniformFanOutShape[(ConsumerRecordMetadata[String], ShoppingCartCmd), (ConsumerRecordMetadata[String],
        ShoppingCartCmd)] = builder.add(Broadcast[(ConsumerRecordMetadata[String], ShoppingCartCmd)](2))

      bcast ~> onlyVal ~> cmdSink
      bcast ~> toEvt ~> bndStmt ~> sink
      SinkShape(bcast.in)
    })
  }

  /** Combines Kafka composite source with composite parallel Cassandra sinks into a RunnableGraph
    *
    * @param dispatcher
    * @param session
    * @param prepStmts map of PreparedStatements used by Cassandra stages
    * @param ec implicit ExecutionContextExecutor
    * @param logger implicit Logger
    * @return
    */
  def shoppingCartCmdRG(dispatcher: Attributes, session: Session, prepStmts: Map[String, PreparedStatement])
    (implicit ec: ExecutionContextExecutor, logger: LoggingAdapter): RunnableGraph[NotUsed] = {
    val cmdEvtSource = shoppingCartCmdEvtSource(dispatcher: Attributes)
    val cmdAndEvtSinks = shoppingCartCmdEvtSinks(dispatcher, session, prepStmts)
    cmdEvtSource.to(cmdAndEvtSinks)
  }
}
