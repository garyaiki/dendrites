/**
  */
package org.gs.examples.account

import org.scalatest.WordSpecLike
import org.gs.filters._
import org.gs.examples.fixtures.AccountTypesBuilder

/**
  * @author Gary Struthers
  */
class AccountTypeSpec extends WordSpecLike with AccountTypesBuilder {

  "An AccountType ProductFilter" should {
    "match subtypes of AccountType" in {
      assert(isAccountType(Checking) === true)
      assert(isAccountType(MoneyMarket) === true)
      assert(isAccountType(Savings) === true)
    }
    "match subtypes of AccountBalances" in {
      val cb = CheckingAccountBalances[BigDecimal](Some(List((3,3000.3), (33,3300.33), (333,3.33))))
      assert(isAccountBalances(cb) === true)
      val cn = CheckingAccountBalances[BigDecimal](None)
      assert(isAccountBalances(cn) === true)
      val mmb = MoneyMarketAccountBalances[Double](Some(List((3,3.3), (33, 33.33), (333,33.00))))
      assert(isAccountBalances(mmb) === true)
      val sb = SavingsAccountBalances[Double](Some(List((3,3.3), (33, 33.33), (333,33.00))))
      assert(isAccountBalances(sb) === true)
    }

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
