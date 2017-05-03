package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.stream

import akka.NotUsed

import akka.stream.ActorAttributes
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.datastax.driver.core.{BoundStatement, PreparedStatement, ResultSet, Row}
import com.datastax.driver.core.utils.UUIDs.timeBased
import java.util.UUID
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import com.github.garyaiki.dendrites.cassandra.{getConditionalError, getKeyspacesNames, sessionLogInfo}
import com.github.garyaiki.dendrites.cassandra.fixtures.BeforeAfterAllBuilder
import com.github.garyaiki.dendrites.cassandra.fixtures.getOneRow
import com.github.garyaiki.dendrites.cassandra.stream.{CassandraBoundQuery, CassandraMappedPaging}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.{CassandraShoppingCart,
  CassandraShoppingCartEvtLog, RetryConfig, ShoppingCartConfig}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCartEvtLog.{bndQuery,
  mapRows, prepQuery}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.ShoppingCartCmd
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.event.ShoppingCartEvt
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.fixtures.ShoppingCartCmdBuilder

class ShoppingCartCmdAndEvtSinkSpec extends WordSpecLike with Matchers with BeforeAndAfterAll
  with BeforeAfterAllBuilder with ShoppingCartCmdBuilder {

  val dispatcher = ActorAttributes.dispatcher("dendrites.blocking-dispatcher")
  val startTime = timeBased
  var evtQueryPrepStmt: PreparedStatement = null
  var prepStmts: Map[String, PreparedStatement] = null

  override def beforeAll() {
    createClusterSchemaSession(ShoppingCartConfig, 1)
    val keyspacesStr = getKeyspacesNames(session)
    CassandraShoppingCart.createTable(session, schema)
    queryPrepStmt = CassandraShoppingCart.prepQuery(session, schema)
    CassandraShoppingCartEvtLog.createTable(session, schema)
    evtQueryPrepStmt = CassandraShoppingCartEvtLog.prepQuery(session, schema)
    val insPrepStmt = CassandraShoppingCart.prepInsert(session, schema)
    val delPrepStmt = CassandraShoppingCart.prepDelete(session, schema)
    val updateOwnerPrepStmt = CassandraShoppingCart.prepUpdateOwner(session, schema)
    val updateItemsPrepStmt = CassandraShoppingCart.prepUpdateItems(session, schema)
    val insEvtPrepStmt = CassandraShoppingCartEvtLog.prepInsert(session, schema)
    prepStmts = Map("Insert" -> insPrepStmt, "SetOwner" -> updateOwnerPrepStmt, "SetItem" -> updateItemsPrepStmt,
      "Delete" -> delPrepStmt, "Query" -> queryPrepStmt, "InsertEvt" -> insEvtPrepStmt)
  }

  "A ShoppingCart Command and Event log" should {

    "poll commands, execute them and save events to Cassandra" in {
      val iter = kvCmds.to[collection.immutable.Iterable]
      val source = Source[(String, ShoppingCartCmd)](iter)
      val sink = shoppingCartCmdEvtSink(dispatcher, session, prepStmts)
      val rg = source.to(sink)
      rg.run()
      Thread.sleep(500) // wait for Cassandra
    }

    "find updated ShoppingCartCmd in Cassandra" in {
      import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCart.{bndQuery, mapRow}
      val source = TestSource.probe[UUID]
      val query = new CassandraBoundQuery[UUID](session, queryPrepStmt, bndQuery, 1)
      def sink = TestSink.probe[ResultSet]
      val (pub, sub) = source.via(query).toMat(sink)(Keep.both).run()
      val row = getOneRow(cartId, (pub, sub))
      pub.sendComplete()
      sub.expectComplete()

      val responseShoppingCart = mapRow(row)
      responseShoppingCart.cartId shouldBe cartId
      responseShoppingCart.owner shouldBe firstOwner
      val items = responseShoppingCart.items
      items.get(firstItem) match {
        case Some(x) => x shouldBe 1
        case None    => fail(s"ShoppingCart firstItem:$firstItem not found")
      }
      items.get(secondItem) match {
        case Some(x) => x shouldBe 2
        case None    => fail(s"ShoppingCart secondItem:secondItem not found")
      }
      responseShoppingCart.version shouldBe 9
    }

    "query by eventId and time" in {
      import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCartEvtLog.{bndQuery,
        mapRows, prepQuery}
      val source = TestSource.probe[(UUID, UUID)]
      val bndStmt = new CassandraBoundQuery(session, evtQueryPrepStmt, bndQuery, 10)
      val paging = new CassandraMappedPaging[ShoppingCartEvt](10, mapRows)
      def sink = TestSink.probe[Seq[ShoppingCartEvt]]
      val (pub, sub) = source.via(bndStmt).via(paging).toMat(sink)(Keep.both).run()
      sub.request(1)
      pub.sendNext((cartId, startTime))
      var response = sub.expectNext()
      response.length shouldBe kvCmds.length
      pub.sendComplete()
      sub.expectComplete()
    }
  }

  override def afterAll() { dropSchemaCloseSessionCluster() }
}