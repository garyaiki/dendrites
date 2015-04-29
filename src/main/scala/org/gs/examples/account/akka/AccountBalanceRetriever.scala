package org.gs.examples.account.akka

import scala.collection._
import scala.concurrent.duration._
import scala.math.BigDecimal.int2bigDecimal
import scala.reflect.runtime.universe._

import akka.actor._
import akka.contrib.pattern.Aggregator
import org.gs._
import org.gs.akka.aggregator.{CantUnderstand, TimedOut}
import org.gs.examples.account._
import org.gs.filters._
import org.gs.reflection._
import shapeless._

/** Sample and test code for the aggregator patter.
  * This is based on Jamie Allen's tutorial at
  * http://jaxenter.com/tutorial-asynchronous-programming-with-akka-actors-46220.html
  */






class AccountBalanceRetriever extends Actor with Aggregator with ActorLogging {

  import context._

  expectOnce {
    case GetCustomerAccountBalances(id, types) ⇒
      new AccountAggregator(sender(), id, accountTypes)
    case _ ⇒
      sender() ! CantUnderstand
      context.stop(self)
  }
  import scala.collection.mutable.ArrayBuffer
  class AccountAggregator(originalSender: ActorRef,
                          id: Long, types: Set[AccountType]) {
    val results = ArrayBuffer.fill[Product](3)(None)

    if (types.size > 0)
      types foreach {
        case Checking    ⇒ fetchCheckingAccountsBalance()
        case Savings     ⇒ fetchSavingsAccountsBalance()
        case MoneyMarket ⇒ fetchMoneyMarketAccountsBalance()
      }
    else collectBalances()

    context.system.scheduler.scheduleOnce(1.second, self, TimedOut)
    expect {
      case TimedOut ⇒ collectBalances(force = true)
    }

    def fetchCheckingAccountsBalance() {
      context.actorOf(Props[CheckingAccountProxy]) ! GetAccountBalances(id)
      expectOnce {
        case CheckingAccountBalances(balances) ⇒
          results.update(0, (Checking -> balances))
          collectBalances()
      }
    }

    def fetchSavingsAccountsBalance() {
      context.actorOf(Props[SavingsAccountProxy]) ! GetAccountBalances(id)
      expectOnce {
        case SavingsAccountBalances(balances) ⇒
          results.update(1, (Savings -> balances))
          collectBalances()
      }
    }

    def fetchMoneyMarketAccountsBalance() {
      context.actorOf(Props[MoneyMarketAccountProxy]) ! GetAccountBalances(id)
      expectOnce {
        case MoneyMarketAccountBalances(balances) ⇒
          results.update(2, (MoneyMarket -> balances))
          collectBalances()
      }
    }

    def collectBalances(force: Boolean = false) {
      val resultCount = results.count(_ != None)
      if ((resultCount == types.size) || force) {
        val result = results.toIndexedSeq
        log.debug(s"$result:${weakParamInfo(result)} cnt:$resultCount ts:${types.size} frc:$force")
        originalSender ! result // Make sure it's immutable
        //context.stop(self)
      }
    }
  }
}