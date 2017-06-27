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
package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.fixtures

import akka.stream.ActorAttributes
import com.datastax.driver.core.{PreparedStatement, Session}
import com.datastax.driver.core.utils.UUIDs.endOf
import java.util.UUID
import org.apache.kafka.common.record.TimestampType
import org.scalatest.{Outcome, TestSuite, TestSuiteMixin}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.{CassandraShoppingCart,
  CassandraShoppingCartEvtLog}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCart.createTable
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCartEvtLog.{createTable =>
  createEvtTable}
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.ShoppingCartCmd
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.event.ShoppingCartEvt
import com.github.garyaiki.dendrites.kafka.ConsumerRecordMetadata
trait ShoppingCartCmdBuilder extends TestSuiteMixin { this: TestSuite =>

  abstract override def withFixture(test: NoArgTest): Outcome = { super.withFixture(test) }

  val dispatcher = ActorAttributes.dispatcher("dendrites.blocking-dispatcher")
  val now = System.currentTimeMillis
  val nowUUID = endOf(now)
  val cartId = UUID.randomUUID
  val firstOwner = UUID.randomUUID
  val secondOwner = UUID.randomUUID
  val firstItem = UUID.randomUUID
  val secondItem = UUID.randomUUID

  val cmds = Seq(ShoppingCartCmd("Insert", cartId, firstOwner, None),
    ShoppingCartCmd("SetOwner", cartId, secondOwner, None),
    ShoppingCartCmd("AddItem", cartId, firstItem, Some(1)),
    ShoppingCartCmd("AddItem", cartId, secondItem, Some(1)),
    ShoppingCartCmd("AddItem", cartId, firstItem, Some(1)),
    ShoppingCartCmd("AddItem", cartId, secondItem, Some(1)),
    ShoppingCartCmd("AddItem", cartId, secondItem, Some(1)),
    ShoppingCartCmd("SetOwner", cartId, firstOwner, None),
    ShoppingCartCmd("RemoveItem", cartId, firstItem, Some(1)),
    ShoppingCartCmd("RemoveItem", cartId, secondItem, Some(1)))
  // Should be firstOwner, firstItem = 1, secondItem = 2
  val kvCmds = for {
    cmd <- cmds
  } yield (ConsumerRecordMetadata("", 0, 0L, now, TimestampType.LOG_APPEND_TIME, UUID.randomUUID.toString), cmd)

  val evts = Seq(ShoppingCartEvt(UUID.randomUUID, cartId, nowUUID, Some(firstOwner), Some(firstItem), Some(1)),
    ShoppingCartEvt(UUID.randomUUID, cartId, nowUUID, None, Some(firstItem), Some(1)),
    ShoppingCartEvt(UUID.randomUUID, cartId, nowUUID, Some(secondOwner), None, None))

  def createTables(session: Session, schema: String): Unit = {
    createTable(session, schema)
    createEvtTable(session, schema)
  }

  def prepareStatements(session: Session, schema: String): Map[String, PreparedStatement] = {
    val insPrepStmt = CassandraShoppingCart.prepInsert(session, schema)
    val updateOwnerPrepStmt = CassandraShoppingCart.prepUpdateOwner(session, schema)
    val updateItemsPrepStmt = CassandraShoppingCart.prepUpdateItems(session, schema)
    val queryPrepStmt = CassandraShoppingCart.prepQuery(session, schema)
    val delPrepStmt = CassandraShoppingCart.prepDelete(session, schema)
    val insEvtPrepStmt = CassandraShoppingCartEvtLog.prepInsert(session, schema)
    val evtQueryPrepStmt = CassandraShoppingCartEvtLog.prepQuery(session, schema)

    Map("Insert" -> insPrepStmt, "SetOwner" -> updateOwnerPrepStmt, "SetItem" -> updateItemsPrepStmt,
      "Delete" -> delPrepStmt, "Query" -> queryPrepStmt, "InsertEvt" -> insEvtPrepStmt, "QueryEvt" -> evtQueryPrepStmt)
  }
}
