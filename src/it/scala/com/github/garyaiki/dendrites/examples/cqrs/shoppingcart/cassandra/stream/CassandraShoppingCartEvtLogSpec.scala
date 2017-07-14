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

import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.datastax.driver.core.PreparedStatement
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.immutable.Iterable
import com.github.garyaiki.dendrites.cassandra.fixtures.BeforeAfterAllBuilder
import com.github.garyaiki.dendrites.cassandra.stream.CassandraSink
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.event.ShoppingCartEvt
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.{ShoppingCartConfig,
  CassandraShoppingCartEvtLog}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCartEvtLog.bndInsert
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.fixtures.{ShoppingCartBehaviors, ShoppingCartCmdBuilder}

class CassandraShoppingCartEvtLogSpec extends WordSpecLike with Matchers with BeforeAndAfterAll
  with BeforeAfterAllBuilder with ShoppingCartCmdBuilder with ShoppingCartBehaviors {

  var prepStmts: Map[String, PreparedStatement] = null

  override def beforeAll() {
    createClusterSchemaSession(ShoppingCartConfig, 1)
    createTables(session, schema)
    prepStmts = prepareStatements(session, schema)
  }

  "A Cassandra ShoppingCartEvtLog client" should {
    "insert ShoppingCartEvt " in {
      val iter = Iterable(evts.toSeq: _*)
      val source = Source[ShoppingCartEvt](iter)
      val prepStmt = prepStmts.get("InsertEvt") match {
        case Some(stmt) => stmt
        case None       => fail("CassandraShoppingCartEvtLog InsertEvt PreparedStatement not found")
      }
      val partialBndInsert = bndInsert(prepStmt, _: ShoppingCartEvt)
      val sink = new CassandraSink(session)
      source.map(partialBndInsert).runWith(sink)
      Thread.sleep(500)
    }
  }

  "query by eventId and time" in {
    val response = queryShoppingCartEvent(session, prepStmts)
    response should contain allElementsOf evts
  }

  override def afterAll() { dropSchemaCloseSessionCluster() }
}
