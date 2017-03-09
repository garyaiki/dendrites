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
import com.github.garyaiki.dendrites.cqrs.Event

package object event {
// Events
case class OwnerChanged(cartId: UUID, time: UUID, eventID: UUID, owner: UUID) extends Event with Cart
case class ItemAdded(cartId: UUID, time: UUID, eventID: UUID, item: UUID, count: Int) extends Event with Cart
case class ItemRemoved(cartId: UUID, time: UUID, eventID: UUID, item: UUID, count: Int) extends Event with Cart
// Event log
case class ShoppingCartEvt(cartId: UUID, time: UUID, eventID: UUID, owner: Option[UUID], item: Option[UUID],
  count: Option[Int]) extends Event with Cart
}