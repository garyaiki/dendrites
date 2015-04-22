package org.gs.akka.aggregator

import scala.collection._
import scala.concurrent.duration._
import scala.math.BigDecimal.int2bigDecimal
import scala.reflect.runtime.universe._

import akka.actor._
import akka.contrib.pattern.Aggregator
import org.gs._
import org.gs.filters._
import shapeless._

/** Sample and test code for the aggregator patter.
  * This is based on Jamie Allen's tutorial at
  * http://jaxenter.com/tutorial-asynchronous-programming-with-akka-actors-46220.html
  */

sealed trait AccountType
case object Checking extends AccountType
case object Savings extends AccountType
case object MoneyMarket extends AccountType

final case class GetCustomerAccountBalances(id: Long, accountTypes: Set[AccountType])
final case class GetAccountBalances(id: Long)

final case class AccountBalances(accountType: AccountType,
                                 balance: Option[List[(Long, BigDecimal)]])

final case class CheckingAccountBalances(balances: Option[List[(Long, BigDecimal)]])
final case class SavingsAccountBalances(balances: Option[List[(Long, BigDecimal)]])
final case class MoneyMarketAccountBalances(balances: Option[List[(Long, BigDecimal)]])

class SavingsAccountProxy extends Actor {
  def receive = {
    case GetAccountBalances(id: Long) ⇒
      sender() ! SavingsAccountBalances(Some(List((1, 150000), (2, 29000))))
  }
}
class CheckingAccountProxy extends Actor {
  def receive = {
    case GetAccountBalances(id: Long) ⇒
      sender() ! CheckingAccountBalances(Some(List((3, 15000))))
  }
}
class MoneyMarketAccountProxy extends Actor {
  def receive = {
    case GetAccountBalances(id: Long) ⇒
      sender() ! MoneyMarketAccountBalances(None)
  }
}

class AccountBalanceRetriever extends Actor with Aggregator with ActorLogging {

  import context._

  expectOnce {
    case GetCustomerAccountBalances(id, types) ⇒
      new AccountAggregator(sender(), id, types)
    case _ ⇒
      sender() ! CantUnderstand
      context.stop(self)
  }
  import scala.collection.mutable.ArrayBuffer
  class AccountAggregator(originalSender: ActorRef,
                          id: Long, types: Set[AccountType]) {
    val results = ArrayBuffer.fill[Product](3)(None)
    var resultCount = 0
    //    var results: Aggregate[HList] = Aggregate()
    //      mutable.ArrayBuffer.empty[(AccountType, Option[List[(Long, BigDecimal)]])]

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
          resultCount += 1
          collectBalances()
      }
    }

    def fetchSavingsAccountsBalance() {
      context.actorOf(Props[SavingsAccountProxy]) ! GetAccountBalances(id)
      expectOnce {
        case SavingsAccountBalances(balances) ⇒
          results.update(1, (Savings -> balances))
          resultCount += 1
          collectBalances()
      }
    }

    def fetchMoneyMarketAccountsBalance() {
      context.actorOf(Props[MoneyMarketAccountProxy]) ! GetAccountBalances(id)
      expectOnce {
        case MoneyMarketAccountBalances(balances) ⇒
          results.update(2, (MoneyMarket -> balances))
          resultCount += 1
          collectBalances()
      }
    }

    def collectBalances(force: Boolean = false) {
      if ((resultCount == types.size) || force) {
        val result = results.toIndexedSeq
        log.debug(s"$result:${weakParamInfo(result)} cnt:$resultCount ts:${types.size} frc:$force")
        originalSender ! result // Make sure it's immutable
        //context.stop(self)
      }
    }
  }
}