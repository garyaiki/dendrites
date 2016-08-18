/** Copyright 2016 Gary Struthers

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
package org.gs.examples.account.http

import akka.actor.{ActorSystem}
import akka.stream.ActorMaterializer
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.Matchers._
import org.gs.examples.account.{CheckingAccountBalances,
      GetAccountBalances,
      MoneyMarketAccountBalances,
      SavingsAccountBalances}

/**
  *
  * @author Gary Struthers
  */
class BalancesProtocolsSpec extends BalancesProtocols with WordSpecLike{
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()

  "An AccountBalancesFormat" should {
    "produce the expected Json" in {
      val jsVal = this.getAccountBalancesFormat.write(GetAccountBalances(1L))
      jsVal.toString() shouldBe """{"id":1}"""
    }
  }

  "A CheckingAccountBalancesFormat" should {
    "produce the expected Json" in {
      val jsVal = this.checkingAccountBalancesFormat.write(
        CheckingAccountBalances[BigDecimal](Some(List((3,3000.3), (33,3300.33), (333,3330.33)))))
      jsVal.toString() shouldBe """{"balances":[[3,3000.3],[33,3300.33],[333,3330.33]]}"""
    }
  }

  "A MoneyMarketAccountBalancesFormat" should {
    "produce the expected Json" in {
      val jsVal = this.moneyMarketAccountBalancesFormat.write(
        MoneyMarketAccountBalances[BigDecimal](
            Some(List((3,33000.3), (33,33300.33), (333,33330.33)))))
      jsVal.toString() shouldBe """{"balances":[[3,33000.3],[33,33300.33],[333,33330.33]]}"""
    }
  }

  "A SavingsAccountBalancesFormat" should {
    "produce the expected Json" in {
      val jsVal = this.savingsAccountBalancesFormat.write(
        SavingsAccountBalances[BigDecimal](
            Some(List((3,333000.3), (33,333300.33), (333,333330.33)))))
      jsVal.toString() shouldBe """{"balances":[[3,333000.3],[33,333300.33],[333,333330.33]]}"""
    }
  }
}
