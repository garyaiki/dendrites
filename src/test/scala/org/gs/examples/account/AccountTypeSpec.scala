/**
  */
package org.gs.examples.account

import org.scalatest.FlatSpecLike
import org.gs.filters._
import org.gs.examples.fixtures.AccountTypesBuilder

/** @author garystruthers
  *
  */
class AccountTypeSpec extends FlatSpecLike with AccountTypesBuilder {

  
  "An AccountType ProductFilter" should "return all AccBalance elements" in {
    val filtered = accountBalances.filter(isAccBalances)

    assert(accountBalances.size === filtered.size)
  }

  it should "return all id elements" in {
    assert(accIds.forall(isLong))
  }
  
  it should "return all value elements" in {
    assert(accVals.forall(isBigDecimal))
  }
}