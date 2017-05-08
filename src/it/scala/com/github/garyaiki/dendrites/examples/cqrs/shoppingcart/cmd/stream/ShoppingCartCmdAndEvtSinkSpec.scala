package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.stream

import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.datastax.driver.core.PreparedStatement
import java.util.UUID
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import com.github.garyaiki.dendrites.cassandra.fixtures.BeforeAfterAllBuilder
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.ShoppingCartConfig
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.ShoppingCartCmd
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.event.ShoppingCartEvt
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.fixtures.{ShoppingCartBehaviors, ShoppingCartCmdBuilder}

class ShoppingCartCmdAndEvtSinkSpec extends WordSpecLike with Matchers with BeforeAndAfterAll
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
      val sink = shoppingCartCmdEvtSink(dispatcher, session, prepStmts)
      val rg = source.to(sink)
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
      val response = queryShoppingCartEvent(session, prepStmts)
      response.length shouldBe kvCmds.length
    }
  }

  override def afterAll() { dropSchemaCloseSessionCluster() }
}