package org.gs.examples.account.http

import akka.actor.{ActorSystem}
import akka.stream.ActorMaterializer
import org.scalatest.{ WordSpecLike, Matchers }
import org.scalatest._
import org.scalatest.Matchers._
import org.gs.examples.account.{CheckingAccountBalances, GetAccountBalances,
    MoneyMarketAccountBalances, SavingsAccountBalances}
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
      jsVal.toString() should equal("""{"id":1}""")
    }
  }

  "A CheckingAccountBalancesFormat" should {
    "produce the expected Json" in {
      val jsVal = this.checkingAccountBalancesFormat.write(
        CheckingAccountBalances[BigDecimal](Some(List((3,3000.3), (33,3300.33), (333,3330.33)))))
      jsVal.toString() should equal("""{"balances":[[3,3000.3],[33,3300.33],[333,3330.33]]}""")
    }
  }

  "A MoneyMarketAccountBalancesFormat" should {
    "produce the expected Json" in {
      val jsVal = this.moneyMarketAccountBalancesFormat.write(
        MoneyMarketAccountBalances[BigDecimal](
            Some(List((3,33000.3), (33,33300.33), (333,33330.33)))))
      jsVal.toString() should equal("""{"balances":[[3,33000.3],[33,33300.33],[333,33330.33]]}""")
    }
  }

  "A SavingsAccountBalancesFormat" should {
    "produce the expected Json" in {
      val jsVal = this.savingsAccountBalancesFormat.write(
        SavingsAccountBalances[BigDecimal](
            Some(List((3,333000.3), (33,333300.33), (333,333330.33)))))
      jsVal.toString() should equal("""{"balances":[[3,333000.3],[33,333300.33],[333,333330.33]]}""")
    }
  }
}
