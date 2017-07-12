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
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.datastax.driver.core.{PreparedStatement, ResultSet}
import java.util.UUID
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.immutable.Iterable
import com.github.garyaiki.dendrites.cassandra.fixtures.BeforeAfterAllBuilder
import com.github.garyaiki.dendrites.cassandra.stream.CassandraRetrySink
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.{RetryConfig, ShoppingCartConfig}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.ShoppingCartCmd
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.doShoppingCartCmd
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.fixtures.{ShoppingCartBehaviors, ShoppingCartCmdBuilder}
import com.github.garyaiki.dendrites.stream.SpyFlow

class CassandraShoppingCartCmdSpec extends WordSpecLike with Matchers with BeforeAndAfterAll
  with BeforeAfterAllBuilder with ShoppingCartBehaviors {

  var prepStmts: Map[String, PreparedStatement] = null

  override def beforeAll() {
    createClusterSchemaSession(ShoppingCartConfig, 1)
    createTables(session, schema)
    prepStmts = prepareStatements(session, schema)
  }

  "A Cassandra ShoppingCartCmd client" should {
    "insert ShoppingCart, set owners & items " in {
      val curriedDoCmd = doShoppingCartCmd(session, prepStmts) _
      val iter = Iterable(cmds.toSeq: _*)
      val source = Source[ShoppingCartCmd](iter)
      //val spy = new SpyFlow[ShoppingCartCmd]("ShoppingCartCmd spy 1", 0, 0)

      val sink = new CassandraRetrySink[ShoppingCartCmd](RetryConfig, curriedDoCmd).withAttributes(dispatcher)
      source.runWith(sink)
      Thread.sleep(500) // wait for Cassandra
    }
  }

  "query a ShoppingCart after updating items and owner" in {
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

  "delete ShoppingCart " in { deleteShoppingCart(session, prepStmts) }

  override def afterAll() { dropSchemaCloseSessionCluster() }
}
