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
package com.github.garyaiki.dendrites.examples.account

import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import com.github.garyaiki.dendrites.examples.fixtures.AccountTypesBuilder

/**
  * @author Gary Struthers
  */
class AccountTypeSpec extends WordSpecLike with AccountTypesBuilder {

  "An AccountType ProductFilter" should {
    "match subtypes of AccountType" in {
      isAccountType(Checking) shouldBe true
      isAccountType(MoneyMarket) shouldBe true
      isAccountType(Savings) shouldBe true
    }

    "match subtypes of AccountBalances" in {
      val cb = CheckingAccountBalances[BigDecimal](Some(List((3,3000.3), (33,3300.33), (333,3.33))))
      isAccountBalances(cb) shouldBe true
      val cn = CheckingAccountBalances[BigDecimal](None)
      isAccountBalances(cn) shouldBe true
      val mmb = MoneyMarketAccountBalances[Double](Some(List((3,3.3), (33, 33.33), (333,33.00))))
      isAccountBalances(mmb) shouldBe true
      val sb = SavingsAccountBalances[Double](Some(List((3,3.3), (33, 33.33), (333,33.00))))
      isAccountBalances(sb) shouldBe true
    }

    "return all AccBalance elements" in {
      val filtered = accountBalances.filter(isAccBalances)
      accountBalances.size shouldBe filtered.size
    }

    "return all id elements" in { accIds.forall(_.isInstanceOf[Long]) }

    "return all value elements" in { accVals.forall(_.isInstanceOf[BigDecimal]) }
  }
}
