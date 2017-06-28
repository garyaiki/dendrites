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
package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.event

import com.datastax.driver.core.utils.UUIDs.{random, startOf, timeBased}
import org.scalatest.{ Matchers, WordSpecLike }

class CompensateDupsSpec extends WordSpecLike with Matchers {
  "A Seq of ShoppingCartEvt" should {
    "return None if it has no duplicates" in {
      val cartId = random
      val time = timeBased
      val ownerId = random
      val newOwnerId = random
      val itemId = random
      val cartEvts = Seq(ShoppingCartEvt(random, random, cartId, time, Some(ownerId), Some(itemId), Some(1)),
        ShoppingCartEvt(random, random, cartId, time, None, Some(itemId), Some(1)),
        ShoppingCartEvt(random, random, cartId, time, Some(newOwnerId), None, None))
      val compensatedEvts = compensateDupEvt(cartEvts)
      compensatedEvts shouldBe None
    }
    "return None if it only duplicates setOwner" in {
      val cartId = random
      val time = timeBased
      val dupEvt = random
      val ownerId = random
      val newOwnerId = random
      val itemId = random
      val cartEvts = Seq(ShoppingCartEvt(random, random, cartId, time, Some(ownerId), Some(itemId), Some(1)),
        ShoppingCartEvt(random, dupEvt, cartId, time, Some(newOwnerId), None, None),
        ShoppingCartEvt(random, dupEvt, cartId, time, Some(newOwnerId), None, None))
      val compensatedEvts = compensateDupEvt(cartEvts)
      compensatedEvts shouldBe None
    }
    "return count of -1 if it duplicates addItem" in {
      val cartId = random
      val time = timeBased
      val dupEvt = random
      val ownerId = random
      val itemId = random
      val cartEvts = Seq(ShoppingCartEvt(random, random, cartId, time, Some(ownerId), Some(itemId), Some(1)),
        ShoppingCartEvt(random, dupEvt, cartId, time, None, Some(itemId), Some(1)),
        ShoppingCartEvt(random, dupEvt, cartId, time, None, Some(itemId), Some(1)))
      val compensatedEvts = compensateDupEvt(cartEvts)
      compensatedEvts.size shouldBe 1
      val evts = compensatedEvts.get
      evts(0) match {
        case ShoppingCartEvt(_, dupEvt, cartId, _, None, Some(itemId), Some(-1)) => succeed
        case ShoppingCartEvt(_, dupEvt, cartId, _, _, Some(itemId), Some(-1)) =>
          fail(s"expected owner:None found owner:${evts(0).owner}")
        case ShoppingCartEvt(_, dupEvt, cartId, _, None, _, Some(-1)) =>
          fail(s"expected item:Some($itemId) found item:${evts(0).item}")
        case ShoppingCartEvt(_, dupEvt, cartId, _, None, Some(itemId), _) =>
          fail(s"expected count:Some(-1) found count:${evts(0).count}")
        case _ => fail(s"wrong compensatedEvent:${evts(0)}")
      }
    }
    "return count of 1 if it duplicates removeItem" in {
      val cartId = random
      val time = timeBased
      val dupEvt = random
      val ownerId = random
      val itemId = random
      val cartEvts = Seq(ShoppingCartEvt(random, random, cartId, time, Some(ownerId), Some(itemId), Some(1)),
        ShoppingCartEvt(random, dupEvt, cartId, time, None, Some(itemId), Some(-1)),
        ShoppingCartEvt(random, dupEvt, cartId, time, None, Some(itemId), Some(-1)))
      val compensatedEvts = compensateDupEvt(cartEvts)
      compensatedEvts.size shouldBe 1
      val evts = compensatedEvts.get
      evts(0) match {
        case ShoppingCartEvt(_, dupEvt, cartId, _, None, Some(itemId), Some(1)) => succeed
        case ShoppingCartEvt(_, dupEvt, cartId, _, _, Some(itemId), Some(1)) =>
          fail(s"expected owner:None found owner:${evts(0).owner}")
        case ShoppingCartEvt(_, dupEvt, cartId, _, None, _, Some(1)) =>
          fail(s"expected item:Some($itemId) found item:${evts(0).item}")
        case ShoppingCartEvt(_, dupEvt, cartId, _, None, Some(itemId), _) =>
          fail(s"expected count:Some(1) found count:${evts(0).count}")
        case _ => fail(s"wrong compensatedEvent:${evts(0)}")
      }
    }
  }
}
