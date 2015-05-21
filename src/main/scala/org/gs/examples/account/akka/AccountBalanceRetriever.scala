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
import org.gs.examples.account.akka.AccountBalanceRetriever._

/** Sample and test code for the aggregator patter.
  * This is based on Jamie Allen's tutorial at
  * http://jaxenter.com/tutorial-asynchronous-programming-with-akka-actors-46220.html
  */

class AccountBalanceRetriever extends Actor
  with MoneyMarketAccountFetcher
  with SavingsAccountFetcher
  with CheckingAccountFetcher
  with ResultAggregator
  with Aggregator
  with ActorLogging {

  import context._
  expectOnce {
    case GetCustomerAccountBalances(id, types) ⇒
      new AccountAggregator(sender(), id, accountTypes)
    case _ ⇒
      sender() ! CantUnderstand
      stop(self)
  }
  
  class AccountAggregator(originalSender: ActorRef, id: Long, types: Set[AccountType]) {

    initResults[AccountType](types)
    if (types.size > 0)
      types foreach {
        case Checking    ⇒ fetchCheckingAccountsBalance(context, id, originalSender)
        case Savings     ⇒ fetchSavingsAccountsBalance(context, id, originalSender)
        case MoneyMarket ⇒ fetchMoneyMarketAccountsBalance(context, id, originalSender)
      }
    else collectResults(originalSender)

    system.scheduler.scheduleOnce(1.second, self, TimedOut)
    expect {
      case TimedOut ⇒ collectResults(originalSender, force = true)
    }
  }
}

object AccountBalanceRetriever {
  def props = Props[AccountBalanceRetriever]
  case class GetCustomerAccountBalances(id: Long, accountTypes: Set[AccountType])
}
