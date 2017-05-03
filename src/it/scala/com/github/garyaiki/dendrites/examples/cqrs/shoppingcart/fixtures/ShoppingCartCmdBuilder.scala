/**
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
package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.fixtures

import com.datastax.driver.core.PreparedStatement
import java.util.UUID
import org.scalatest.{ Outcome, TestSuite, TestSuiteMixin }
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.ShoppingCartCmd

trait ShoppingCartCmdBuilder extends TestSuiteMixin { this: TestSuite =>

  abstract override def withFixture(test: NoArgTest): Outcome = { super.withFixture(test) }

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
  } yield (UUID.randomUUID.toString, cmd)

  var queryPrepStmt: PreparedStatement = null
  var delPrepStmt: PreparedStatement = null
}