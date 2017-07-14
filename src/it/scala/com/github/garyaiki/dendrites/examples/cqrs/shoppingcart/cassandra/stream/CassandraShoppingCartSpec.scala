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
package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.stream

import akka.NotUsed
import akka.stream.ActorAttributes
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.datastax.driver.core.{PreparedStatement, Row}
import java.util.UUID
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.immutable.Iterable
import com.github.garyaiki.dendrites.cassandra.getConditionalError
import com.github.garyaiki.dendrites.cassandra.fixtures.BeforeAfterAllBuilder
import com.github.garyaiki.dendrites.cassandra.stream.{CassandraConditional, CassandraRetrySink, CassandraSink}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.{ShoppingCart, SetItems, SetOwner}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.{ShoppingCartConfig, CassandraShoppingCart}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCart.{bndInsert,
  bndUpdateItems, bndUpdateOwner, checkAndSetOwner, rowToString}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.RetryConfig
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.fixtures.{ShoppingCartBehaviors, ShoppingCartCmdBuilder}

class CassandraShoppingCartSpec extends WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAfterAllBuilder
  with ShoppingCartCmdBuilder with ShoppingCartBehaviors {

  var prepStmts: Map[String, PreparedStatement] = null
  var prepQueryStmt: PreparedStatement = null
  val items: Map[UUID, Int] = Map(firstItem -> 1, UUID.randomUUID -> 1)
  val updatedItems: Map[UUID, Int] = items + (firstItem -> 2)
  val cart: ShoppingCart = ShoppingCart(cartId, firstOwner, items)
  val carts: Seq[ShoppingCart] = Seq(cart)
  val setOwner: SetOwner = SetOwner(cartId, secondOwner)
  val setOwners = Seq(setOwner)
  val setItems: SetItems = SetItems(cartId, updatedItems)
  val updatedItemsCart: ShoppingCart = ShoppingCart(cartId, firstOwner, updatedItems, 1)
  val updatedItemsCarts = Seq(updatedItemsCart)
  val updatedCart: ShoppingCart = ShoppingCart(cartId, secondOwner, updatedItems, 2)
  val updatedCarts = Seq(updatedCart)

  override def beforeAll() {
    createClusterSchemaSession(ShoppingCartConfig, 1)
    createTables(session, schema)
    prepStmts = prepareStatements(session, schema)
    prepQueryStmt = prepStmts.get("Query") match {
      case Some(stmt) => stmt
      case None       => fail("CassandraShoppingCart Query PreparedStatement not found")
    }
  }

  "A Cassandra ShoppingCart client" should {
    "insert ShoppingCart " in {
      val iter = Iterable(carts.toSeq: _*)
      val source = Source[ShoppingCart](iter)
      val prepStmt = prepStmts.get("Insert") match {
        case Some(stmt) => stmt
        case None       => fail("CassandraShoppingCart Insert PreparedStatement not found")
      }
      val partialBndInsert = bndInsert(prepStmt, _: ShoppingCart)
      val sink = new CassandraSink(session)
      source.map(partialBndInsert).runWith(sink)
    }
  }

  "query a ShoppingCart" in {
    val response = queryShoppingCart(session, prepStmts)
    response shouldBe carts
  }

  "update a ShoppingCart item" in {
    val source = TestSource.probe[SetItems]
    val prepStmt = prepStmts.get("SetItem") match {
      case Some(stmt) => stmt
      case None       => fail("CassandraShoppingCart SetItem PreparedStatement not found")
    }
    val partialBndUpdateItems = bndUpdateItems(prepStmt, _: SetItems)
    val curriedErrorHandler = getConditionalError(rowToString) _
    val conditional = new CassandraConditional(session, curriedErrorHandler)
    def sink = TestSink.probe[Option[Row]]
    val (pub, sub) = source.map(partialBndUpdateItems).via(conditional).toMat(sink)(Keep.both).run()
    sub.request(1)
    pub.sendNext(setItems)
    val response = sub.expectNext()
    pub.sendComplete()
    sub.expectComplete()

    response shouldBe None
  }

  "query a ShoppingCart after update items" in {
    val response = queryShoppingCart(session, prepStmts)
    response shouldBe updatedItemsCarts
  }

  "check and set a ShoppingCart owner" in {
    val iter = Iterable(setOwners.toSeq: _*)
    val prepStmt = prepStmts.get("SetOwner") match {
      case Some(stmt) => stmt
      case None       => fail("CassandraShoppingCart SetOwner PreparedStatement not found")
    }
    val curriedCheckAndSetOwner = checkAndSetOwner(session, prepQueryStmt, prepStmt) _
    val source = Source[SetOwner](iter)
    val sink = new CassandraRetrySink[SetOwner](RetryConfig, curriedCheckAndSetOwner).withAttributes(dispatcher)
    source.runWith(sink)
  }

  "query a ShoppingCart after updating owner" in {
    val response = queryShoppingCart(session, prepStmts)
    response shouldBe updatedCarts
  }

  "delete ShoppingCart " in { deleteShoppingCart(session, prepStmts) }

  override def afterAll() { dropSchemaCloseSessionCluster() }
}
