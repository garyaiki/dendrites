package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd

import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.{Attributes, SinkShape, UniformFanOutShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.scaladsl.GraphDSL.Implicits._
import com.datastax.driver.core.{PreparedStatement, Session}
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import com.github.garyaiki.dendrites.cassandra.stream.{CassandraBind, CassandraKeyValueFlow, CassandraRetrySink, CassandraSink}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.avro4s.Avro4sShoppingCartCmd.toCaseClass
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.{CassandraShoppingCart,
  CassandraShoppingCartEvtLog, RetryConfig, ShoppingCartConfig}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCartEvtLog.bndInsert
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.kafka.ShoppingCartCmdConsumer
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.event.ShoppingCartEvt
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.event.cmdToEvt
import com.github.garyaiki.dendrites.kafka.stream.avro4s.ConsumerRecordDeserializer
import com.github.garyaiki.dendrites.kafka.stream.{ConsumerRecordsToQueue, KafkaSource}
import com.github.garyaiki.dendrites.kafka.stream.extractRecords
import com.github.garyaiki.dendrites.stream.SpyFlow
import com.datastax.driver.core.BoundStatement

package object stream {

  /** Kafka source polls ShoppingCart command topic, queues Kafka keys and values, deserializes Avro byte arrays to
    * ShoppingCartCmd
    *
    * @param dispatcher
    * @param ec
    * @param logger
    * @return composite source
    */
  def shoppingCartCmdEvtSource(dispatcher: Attributes)
    (implicit ec: ExecutionContext, logger: LoggingAdapter): Source[(String, ShoppingCartCmd), NotUsed] = {

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
    * @param ec
    * @param logger
    * @return composite Cassandra sink
    */
  def shoppingCartCmdEvtSink(dispatcher: Attributes, session: Session, prepStmts: Map[String, PreparedStatement])
    (implicit ec: ExecutionContext, logger: LoggingAdapter): Sink[(String, ShoppingCartCmd), NotUsed] = {

    val curriedDoCmd = doShoppingCartCmd(session, prepStmts) _
    val cmdFlowGraph = new CassandraKeyValueFlow[String, ShoppingCartCmd](RetryConfig, curriedDoCmd)
      .withAttributes(dispatcher)
    val cmdFlow = Flow.fromGraph(cmdFlowGraph)
    val toEvt = Flow.fromFunction[(String, ShoppingCartCmd), ShoppingCartEvt] { x => cmdToEvt(x._1, x._2) }
    val optInsEvtPrepStmt = prepStmts.get("InsertEvt")
    val insEvtPrepStmt = optInsEvtPrepStmt match {
      case Some(x) => x
      case None => throw new NullPointerException("ShoppingCartEvt Insert preparedStatement not found")
    }
    val bndStmt = new CassandraBind(insEvtPrepStmt, bndInsert)
    val sink = new CassandraSink(session)
    //val spyCmd = new SpyFlow[(String, ShoppingCartCmd)]("cmd spy", 0, 0)
    //val spy = Flow.fromGraph(new SpyFlow[(String, ShoppingCartCmd)]("source spy", 0, 0))
    //val spyEvt = new SpyFlow[ShoppingCartEvt]("evt spy", 0, 0)

    cmdFlow.via(toEvt).via(bndStmt).to(sink)//.to(Sink.ignore)
  }

  /** Composite parallel Cassandra sinks. Equivalent to shoppingCartCmdEvtSink but this splits command and event logging
    * into parallel streams. On one branch, a sink executes the command to store ShoppingCartCmd. The other branch
    * maps key/value to ShoppingCartEvt, then binds ShoppingCartEvt to a BoundStatement, then inserts the event in a
    * Cassandra event log
    *
    * @param dispatcher
    * @param session
    * @param prepStmts map of PreparedStatements used by Cassandra stages
    * @param ec
    * @param logger
    * @return composite Cassandra sink
    */
  def shoppingCartCmdEvtSinks(dispatcher: Attributes, session: Session, prepStmts: Map[String, PreparedStatement])
    (implicit ec: ExecutionContext, logger: LoggingAdapter): Sink[(String, ShoppingCartCmd), NotUsed] = {

    val onlyVal = Flow.fromFunction[(String, ShoppingCartCmd), ShoppingCartCmd] { x => x._2 }
    val curriedDoCmd = doShoppingCartCmd(session, prepStmts) _
    val cmdSink = new CassandraRetrySink[ShoppingCartCmd](RetryConfig, curriedDoCmd).withAttributes(dispatcher)

    val toEvt = Flow.fromFunction[(String, ShoppingCartCmd), ShoppingCartEvt] { x => cmdToEvt(x._1, x._2) }
    val optInsEvtPrepStmt = prepStmts.get("InsertEvt")//CassandraShoppingCartEvtLog.prepInsert(session, schema)
    val insEvtPrepStmt = optInsEvtPrepStmt match {
      case Some(x) => x
      case None => throw new NullPointerException("ShoppingCartEvt Insert preparedStatement not found")
    }
    val bndStmt = new CassandraBind(insEvtPrepStmt, bndInsert)
    val sink = new CassandraSink(session)
    //val spy = new SpyFlow[BoundStatement]("evt spy", 0, 0)
    //val spyCmd = new SpyFlow[ShoppingCartCmd]("cmd spy", 0, 0)
    //val spyEvt = new SpyFlow[ShoppingCartEvt]("evt spy", 0, 0)

    Sink.fromGraph(GraphDSL.create() { implicit builder =>
      val bcast: UniformFanOutShape[(String, ShoppingCartCmd), (String, ShoppingCartCmd)] =
        builder.add(Broadcast[(String, ShoppingCartCmd)](2))
      //val onlyVal = Flow.fromFunction[(String, ShoppingCartCmd), ShoppingCartCmd] { x => x._2 }
     // val toEvt = Flow.fromFunction[(String, ShoppingCartCmd), ShoppingCartEvt] { x => cmdToEvt(x._1, x._2) }
      //val sCmd = Flow.fromGraph(spyCmd)
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
    * @param ec
    * @param logger
    * @return
    */
  def shoppingCartCmdRG(dispatcher: Attributes, session: Session, prepStmts: Map[String, PreparedStatement])
    (implicit ec: ExecutionContext, logger: LoggingAdapter): RunnableGraph[NotUsed] = {
    val cmdEvtSource = shoppingCartCmdEvtSource(dispatcher: Attributes)
    val cmdAndEvtSinks = shoppingCartCmdEvtSinks(dispatcher, session, prepStmts)
    cmdEvtSource.to(cmdAndEvtSinks)
  }
}
