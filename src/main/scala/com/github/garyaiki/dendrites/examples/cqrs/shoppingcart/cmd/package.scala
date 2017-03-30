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
package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart

import com.datastax.driver.core.{PreparedStatement, ResultSet, ResultSetFuture ,Session}
import java.util.UUID
import com.github.garyaiki.dendrites.cqrs.Command
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cassandra.CassandraShoppingCart.{bndInsert,
  checkAndSetItems, checkAndSetOwner, bndDelete, prepInsert}

package object cmd {
  case class ShoppingCartCmd(action: String, cartId: UUID, ownerOrItem: UUID, count: Option[Int]) extends Command
    with Cart

  def doShoppingCartCmd(session: Session, stmts: Map[String, PreparedStatement])(cmd: ShoppingCartCmd): ResultSetFuture
    = {
    val action = cmd.action
    action match {
      case _ if action == "Insert" => {
        val ps = stmts.get("Insert")
        ps match {
          case Some(x) => {
            val cc = ShoppingCart(cmd.cartId, cmd.ownerOrItem, Map.empty[UUID, Int])
            val bs = bndInsert(x, cc)
            session.executeAsync(bs)
          }
          case None => throw new NullPointerException("ShoppingCart Insert preparedStatement not found")
        }
      }

      case _ if action == "SetOwner" => {
        val optQueryStmt = stmts.get("Query")
        val queryStmt = optQueryStmt match {
          case Some(x) => x
          case None => throw new NullPointerException("ShoppingCart SetOwner,Query preparedStatement not found")
        }
        val optSetStmt = stmts.get("SetOwner")
        val setStmt = optSetStmt match {
          case Some(x) => x
          case None => throw new NullPointerException("ShoppingCart SetOwner preparedStatement not found")
        }
        val setOwner = SetOwner(cmd.cartId, cmd.ownerOrItem)
        checkAndSetOwner(session, queryStmt, setStmt)(setOwner)
      }

      case _ if action == "AddItem" => {
        val bothStmts = (stmts.get("Query"), stmts.get("SetItem"))
        bothStmts match {
          case (Some(queryStmt), Some(setStmt)) => {
            val setItems = SetItems(cmd.cartId, Map(cmd.ownerOrItem -> cmd.count.get))
            checkAndSetItems(session, queryStmt, setStmt)(setItems)
          }
          case (Some(x), None) => throw new NullPointerException("ShoppingCart SetItem preparedStatement not found")
          case (None, Some(y)) => throw new NullPointerException("ShoppingCart Query preparedStatement not found")
          case (None, None) => throw new NullPointerException("ShoppingCart SetItem,Query preparedStatement not found")
          case _ => throw new NullPointerException("ShoppingCart AddItem, one or both preparedStatements not found")
        }
      }

      case _ if action == "RemoveItem" => {
        val bothStmts = (stmts.get("Query"), stmts.get("SetItem"))
        bothStmts match {
          case (Some(queryStmt), Some(setStmt)) => {
            val setItems = SetItems(cmd.cartId, Map(cmd.ownerOrItem -> (cmd.count.get * -1)))
            checkAndSetItems(session, queryStmt, setStmt)(setItems)
          }
          case (Some(x), None) => throw new NullPointerException("ShoppingCart SetItem preparedStatement not found")
          case (None, Some(y)) => throw new NullPointerException("ShoppingCart Query preparedStatement not found")
          case (None, None) => throw new NullPointerException("ShoppingCart SetItem,Query preparedStatement not found")
          case _ => throw new NullPointerException("ShoppingCart RemoveItem, one or both preparedStatements not found")
        }
      }

      case _ if action == "Delete" => {
        val optStmt = stmts.get("Delete")
        optStmt match {
          case Some(delStmt) => {
            val bs = bndDelete(delStmt, cmd.cartId)
            session.executeAsync(bs)
          }
          case None => throw new NullPointerException("ShoppingCart Delete preparedStatement not found")
        }
      }
    }
  }
}
