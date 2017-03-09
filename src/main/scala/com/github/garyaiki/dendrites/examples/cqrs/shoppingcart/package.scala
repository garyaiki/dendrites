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
package com.github.garyaiki.dendrites.examples.cqrs

import java.util.UUID
import com.github.garyaiki.dendrites.cqrs.Event

package shoppingcart {

  trait Cart { val cartId: UUID }

  case class ShoppingCart(cartId: UUID, owner: UUID, items: Map[UUID, Int], version: Int = 0) extends Cart
  case class SetOwner(cartId: UUID, owner: UUID, version: Int = 0) extends Cart
  case class SetItems(cartId: UUID, items: Map[UUID, Int], version: Int = 0) extends Cart

  sealed trait Query extends Cart
  case class GetItems(cartId: UUID) extends Query

  /*

case class ManagerCommand(cmd: Command, id: Long, replyTo: ActorRef)
case class ManagerEvent(id: Long, event: Event)
case class ManagerQuery(cmd: Query, id: Long, replyTo: ActorRef)
case class ManagerResult(id: Long, result: Result)
case class ManagerRejection(id: Long, reason: String)*/
}