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
    val filtered = filterProducts(accountBalances, productFilter, isAccBalances) //isType[AccBalances])
        println(s"accountBalances size:${accountBalances.size}")
        println(s"filtered size:${filtered.size}")
  /*  val r = 0 until 12
        for (i <- r) {
      println(s"accountBalances:${accountBalances(i)} === filtered:${filtered(i)}")
    }*/
    assert(accountBalances.size === filtered.size)
  }

  it should "return all AccountType elements" in {
    assert(accTypes.forall(isAccountType))
  }

  it should "return all id elements" in {
    assert(accIds.forall(isLong))
  }
  
  it should "return all value elements" in {
    assert(accVals.forall(isBigDecimal))
  }
}