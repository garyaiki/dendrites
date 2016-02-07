/**
  */
package org.gs.examples.account

import org.scalatest.WordSpecLike
import org.gs.filters._
import org.gs.examples.fixtures.AccountTypesBuilder

/** @author garystruthers
  *
  */
class AccountTypeSpec extends WordSpecLike with AccountTypesBuilder {

  "An AccountType ProductFilter" should {
    "return all AccBalance elements" in {
      val filtered = accountBalances.filter(isAccBalances)

      assert(accountBalances.size === filtered.size)
    }

    "return all id elements" in {
      assert(accIds.forall(_.isInstanceOf[Long]))
    }

    "return all value elements" in {
      assert(accVals.forall(_.isInstanceOf[BigDecimal]))
    }
  }
}
