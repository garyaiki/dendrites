/** Copyright 2016 - 2017 Gary Struthers

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

import java.util.UUID
import com.github.garyaiki.dendrites.cqrs.Command

package object cmd {
  case class ShoppingCartCmd(action: String, cartId: UUID, ownerOrItem: UUID, count: Option[Int]) extends Command
    with Cart {

    def fromCmd: Cart = action match {
      case _ if action == "SetOwner"   => SetOwnerCmd(cartId, ownerOrItem)
      case _ if action == "AddItem"    => AddItemCmd(cartId, ownerOrItem, count.get)
      case _ if action == "RemoveItem" => RemoveItemCmd(cartId, ownerOrItem, count.get)
    }
  }

  case class SetOwnerCmd(cartId: UUID, owner: UUID) extends Cart {
    def toCmd: ShoppingCartCmd = ShoppingCartCmd("SetOwner", cartId, owner, None)
  }

  case class AddItemCmd(cartId: UUID, item: UUID, count: Int) extends Cart {
    def toCmd: ShoppingCartCmd = ShoppingCartCmd("AddItem", cartId, item, Some(count))
  }

  case class RemoveItemCmd(cartId: UUID, item: UUID, count: Int) extends Cart {
    def toCmd: ShoppingCartCmd = ShoppingCartCmd("RemoveItem", cartId, item, Some(count))
  }
}