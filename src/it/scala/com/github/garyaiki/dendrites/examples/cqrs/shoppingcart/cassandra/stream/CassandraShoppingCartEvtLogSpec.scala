/**
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
package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.stream

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.datastax.driver.core.{PreparedStatement, ResultSet, Row}
import java.util.UUID
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.collection.immutable.Iterable
import com.github.garyaiki.dendrites.cassandra.fixtures.BeforeAfterAllBuilder
import com.github.garyaiki.dendrites.cassandra.stream.{CassandraBind, CassandraBoundQuery, CassandraMappedPaging,
  CassandraSink}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.event.ShoppingCartEvt
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.{ShoppingCartConfig,
  CassandraShoppingCartEvtLog}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCartEvtLog.{bndInsert,
  bndQuery, mapRows}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.fixtures.ShoppingCartCmdBuilder

class CassandraShoppingCartEvtLogSpec extends WordSpecLike with Matchers with BeforeAndAfterAll
  with BeforeAfterAllBuilder with ShoppingCartCmdBuilder {

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
      val bndStmt = new CassandraBind(prepStmt, bndInsert)
      val sink = new CassandraSink(session)
      source.via(bndStmt).runWith(sink)
      Thread.sleep(500)
    }
  }

  "query by eventId and time" in {
    val source = TestSource.probe[(UUID, UUID)]
    val prepStmt = prepStmts.get("QueryEvt") match {
      case Some(stmt) => stmt
      case None       => fail("CassandraShoppingCartEvtLog QueryEvt PreparedStatement not found")
    }
    val bndStmt = new CassandraBoundQuery(session, prepStmt, bndQuery, 10)
    val paging = new CassandraMappedPaging[ShoppingCartEvt](10, mapRows)
    def sink = TestSink.probe[Seq[ShoppingCartEvt]]
    val (pub, sub) = source.via(bndStmt).via(paging).toMat(sink)(Keep.both).run()
    sub.request(1)
    pub.sendNext((cartId, startTime))
    var response = sub.expectNext()
    response shouldBe evts
    pub.sendComplete()
    sub.expectComplete()
  }

  override def afterAll() { dropSchemaCloseSessionCluster() }
}
