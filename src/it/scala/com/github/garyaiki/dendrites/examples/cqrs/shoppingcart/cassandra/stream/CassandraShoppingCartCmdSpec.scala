/** Licensed under the Apache License, Version 2.0 (the "License");
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
package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.stream

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.datastax.driver.core.{PreparedStatement, ResultSet}
import java.util.UUID
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.immutable.Iterable
import com.github.garyaiki.dendrites.cassandra.fixtures.BeforeAfterAllBuilder
import com.github.garyaiki.dendrites.cassandra.fixtures.getOneRow
import com.github.garyaiki.dendrites.cassandra.stream.{CassandraBind, CassandraBoundQuery, CassandraRetrySink,
  CassandraSink}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.{RetryConfig, ShoppingCartConfig}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCart.{bndDelete, bndQuery,
  mapRow}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.ShoppingCartCmd
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.doShoppingCartCmd
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.fixtures.ShoppingCartCmdBuilder
import com.github.garyaiki.dendrites.stream.SpyFlow

class CassandraShoppingCartCmdSpec extends WordSpecLike with Matchers with BeforeAndAfterAll
    with BeforeAfterAllBuilder with ShoppingCartCmdBuilder {

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
    val source = TestSource.probe[UUID]
    val prepStmt = prepStmts.get("Query") match {
      case Some(stmt) => stmt
      case None       => fail("CassandraShoppingCart Query PreparedStatement not found")
    }
    val query = new CassandraBoundQuery[UUID](session, prepStmt, bndQuery, 1)
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

  "delete ShoppingCart " in {
    val cartIds = Seq(cartId)
    val iter = Iterable(cartIds.toSeq: _*)
    val source = Source[UUID](iter)
    val prepStmt = prepStmts.get("Delete") match {
      case Some(stmt) => stmt
      case None       => fail("CassandraShoppingCart Delete PreparedStatement not found")
    }
    val bndStmt = new CassandraBind(prepStmt, bndDelete)
    val sink = new CassandraSink(session)
    source.via(bndStmt).runWith(sink)
  }

  override def afterAll() { dropSchemaCloseSessionCluster() }
}
