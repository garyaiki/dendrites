package org.gs.examples.account.akka

import scala.collection._
import scala.concurrent.duration._
import scala.math.BigDecimal.int2bigDecimal
import scala.reflect.runtime.universe._
import akka.actor._
import akka.contrib.pattern.Aggregator
import org.gs._
import org.gs.akka.aggregator.{ CantUnderstand, TimedOut }
import org.gs.examples.account._
import org.gs.filters._
import org.gs.reflection._
//import shapeless._
import org.gs.examples.account.akka.AccountBalanceRetriever._

/** Sample and test code for the aggregator patter.
  * This is based on Jamie Allen's tutorial at
  * http://jaxenter.com/tutorial-asynchronous-programming-with-akka-actors-46220.html
  */

object AccountBalanceRetriever {
  def props = Props[AccountBalanceRetriever]
  case class GetCustomerAccountBalances(id: Long, accountTypes: Set[AccountType])

  case class CheckingAccountBalances(balances: Option[List[(Long, BigDecimal)]])
  case class SavingsAccountBalances(balances: Option[List[(Long, BigDecimal)]])
  case class MoneyMarketAccountBalances(balances: Option[List[(Long, BigDecimal)]])
}
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

    def collectBalances(force: Boolean = false) {
      val resultCount = results.count(_ != None)
      if ((resultCount == types.size) || force) {
        val result = results.toIndexedSeq
        //        log.debug(s"$result:${weakParamInfo(result)} cnt:$resultCount ts:${types.size} frc:$force")
        originalSender ! result // Make sure it's immutable
        //context.stop(self)
      }
    }
    //type bu = Boolean => Unit

    if (types.size > 0)
      types foreach {
        case Checking    ⇒ fetchCheckingAccountsBalance(context, id)
        case Savings     ⇒ fetchSavingsAccountsBalance(context, id)
        case MoneyMarket ⇒ fetchMoneyMarketAccountsBalance(context, id)
      }
    else collectBalances()

    context.system.scheduler.scheduleOnce(1.second, self, TimedOut)
    expect {
      case TimedOut ⇒ collectBalances(force = true)
    }
    val cap = CheckingAccountProxy.props
    def fetchCheckingAccountsBalance(context: ActorContext, id: Long) {
      context.actorOf(CheckingAccountProxy.props) ! GetAccountBalances(id)
      expectOnce {
        case CheckingAccountBalances(balances) ⇒
          results.update(0, (Checking -> balances))
          collectBalances()
      }
    }

    def fetchSavingsAccountsBalance(context: ActorContext, id: Long) {
      context.actorOf(Props[SavingsAccountProxy]) ! GetAccountBalances(id)
      expectOnce {
        case SavingsAccountBalances(balances) ⇒
          results.update(1, (Savings -> balances))
          collectBalances()
      }
    }

    def fetchMoneyMarketAccountsBalance(context: ActorContext, id: Long) {
      context.actorOf(Props[MoneyMarketAccountProxy]) ! GetAccountBalances(id)
      expectOnce {
        case MoneyMarketAccountBalances(balances) ⇒
          results.update(2, (MoneyMarket -> balances))
          collectBalances()
      }
    }

  }
}

