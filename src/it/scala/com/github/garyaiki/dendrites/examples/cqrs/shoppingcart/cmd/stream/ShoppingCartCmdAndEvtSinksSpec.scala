package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.stream

import akka.NotUsed
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.datastax.driver.core.{PreparedStatement, ResultSet}
import java.util.UUID
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import com.github.garyaiki.dendrites.cassandra.{getConditionalError, getKeyspacesNames}
import com.github.garyaiki.dendrites.cassandra.fixtures.BeforeAfterAllBuilder
import com.github.garyaiki.dendrites.cassandra.stream.{CassandraBoundQuery, CassandraMappedPaging}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.{CassandraShoppingCart,
  CassandraShoppingCartEvtLog, RetryConfig, ShoppingCartConfig}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCartEvtLog.{bndQuery =>
  evtBndQuery, mapRows}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.ShoppingCartCmd
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.event.ShoppingCartEvt
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.fixtures.{ShoppingCartBehaviors, ShoppingCartCmdBuilder}

class ShoppingCartCmdAndEvtSinksSpec extends WordSpecLike with Matchers with BeforeAndAfterAll
  with BeforeAfterAllBuilder with ShoppingCartCmdBuilder with ShoppingCartBehaviors {

  var prepStmts: Map[String, PreparedStatement] = null

  override def beforeAll() {
    createClusterSchemaSession(ShoppingCartConfig, 1)
    createTables(session, schema)
    prepStmts = prepareStatements(session, schema)
  }

  "A ShoppingCart Command and Event log" should {

    "poll commands, execute them and save events to Cassandra" in {
      val iter = kvCmds.to[collection.immutable.Iterable]
      val source = Source[(String, ShoppingCartCmd)](iter)
      val sinks = shoppingCartCmdEvtSinks(dispatcher, session, prepStmts)
      val rg = source.to(sinks)
      rg.run()
      Thread.sleep(500) // wait for Cassandra
    }

    "find updated ShoppingCartCmd in Cassandra" in {
      val response = queryShoppingCart(session, prepStmts)
      val shoppingCart = response(0)
      shoppingCart.cartId shouldBe cartId
      shoppingCart.owner shouldBe firstOwner
      val items = shoppingCart.items
      items.get(firstItem) match {
        case Some(x) => x shouldBe 1
        case None    => fail(s"ShoppingCart firstItem:$firstItem not found")
      }
      items.get(secondItem) match {
        case Some(x) => x shouldBe 2
        case None    => fail(s"ShoppingCart secondItem:secondItem not found")
      }
      shoppingCart.version shouldBe 9
    }

    "query by eventId and time" in {
      val source = TestSource.probe[(UUID, UUID)]
      val prepStmt = prepStmts.get("QueryEvt") match {
        case Some(stmt) => stmt
        case None => fail("CassandraShoppingCartEvtLog QueryEvt PreparedStatement not found")
      }
      val bndStmt = new CassandraBoundQuery(session, prepStmt, evtBndQuery, 10)
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
