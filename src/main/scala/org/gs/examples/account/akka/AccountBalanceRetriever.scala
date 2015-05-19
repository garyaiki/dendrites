package org.gs.examples.account.akka
import scala.collection.immutable.Set
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.math.BigDecimal.int2bigDecimal
import scala.reflect.runtime.universe._
import akka.actor._
import akka.contrib.pattern.Aggregator
import org.gs._
import org.gs.akka.aggregator.{ CantUnderstand, ResultAggregator, TimedOut }
import org.gs.examples.account._
//import org.gs.filters._
//import org.gs.reflection._
//import shapeless._
import org.gs.examples.account.akka.AccountBalanceRetriever._

/** Sample and test code for the aggregator patter.
  * This is based on Jamie Allen's tutorial at
  * http://jaxenter.com/tutorial-asynchronous-programming-with-akka-actors-46220.html
  */

class AccountBalanceRetriever extends Actor with ResultAggregator with Aggregator with ActorLogging {

  import context._
  expectOnce {
    case GetCustomerAccountBalances(id, types) ⇒
      new AccountAggregator(sender(), id, accountTypes)
    case _ ⇒
      sender() ! CantUnderstand
      context.stop(self)
  }
  class AccountAggregator(originalSender: ActorRef,
                          id: Long, types: Set[AccountType]) {

    initResults[AccountType](types)
    if (types.size > 0)
      types foreach {
        case Checking    ⇒ fetchCheckingAccountsBalance(context, id)
        case Savings     ⇒ fetchSavingsAccountsBalance(context, id)
        case MoneyMarket ⇒ fetchMoneyMarketAccountsBalance(context, id)
      }
    else collectBalances(originalSender)

    context.system.scheduler.scheduleOnce(1.second, self, TimedOut)
    expect {
      case TimedOut ⇒ collectBalances(originalSender, force = true)
    }

    def action(cb: CheckingAccountBalances): Unit = { println("checking account balances") }
    def fetchCheckingAccountsBalance(context: ActorContext, id: Long) {
      context.actorOf(CheckingAccountProxy.props) ! GetAccountBalances(id)
      expectOnce {
        case CheckingAccountBalances(balances) ⇒
          addResult(0, (Checking -> balances), originalSender)
      }
    }

    def fetchSavingsAccountsBalance(context: ActorContext, id: Long) {
      context.actorOf(Props[SavingsAccountProxy]) ! GetAccountBalances(id)
      expectOnce {
        case SavingsAccountBalances(balances) ⇒
          addResult(1, (Savings -> balances), originalSender)
      }
    }

    def fetchMoneyMarketAccountsBalance(context: ActorContext, id: Long) {
      context.actorOf(Props[MoneyMarketAccountProxy]) ! GetAccountBalances(id)
      expectOnce {
        case MoneyMarketAccountBalances(balances) ⇒
          addResult(2, (MoneyMarket -> balances), originalSender)
      }
    }
  }
}

object AccountBalanceRetriever {
  def props = Props[AccountBalanceRetriever]
  case class GetCustomerAccountBalances(id: Long, accountTypes: Set[AccountType])

  case class CheckingAccountBalances(balances: Option[List[(Long, BigDecimal)]])
  case class SavingsAccountBalances(balances: Option[List[(Long, BigDecimal)]])
  case class MoneyMarketAccountBalances(balances: Option[List[(Long, BigDecimal)]])
}
