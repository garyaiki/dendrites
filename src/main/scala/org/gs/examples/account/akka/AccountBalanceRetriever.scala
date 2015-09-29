package org.gs.examples.account.akka

import akka.actor._
import akka.contrib.pattern.Aggregator
import org.gs._
import org.gs.akka.aggregator.{ CantUnderstand, ResultAggregator, TimedOut }
import org.gs.examples.account._
import org.gs.examples.account.akka.AccountBalanceRetriever._
import scala.collection.immutable.Set
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.math.BigDecimal.int2bigDecimal
import scala.reflect.runtime.universe._

/** Sample and test code for the aggregator patter.
  * This is based on Jamie Allen's tutorial at
  * http://jaxenter.com/tutorial-asynchronous-programming-with-akka-actors-46220.html
  */

class AccountBalanceRetriever(actors: Map[AccountType, Props]) extends Actor
  with MoneyMarketAccountFetcher
  with SavingsAccountFetcher
  with CheckingAccountFetcher
  with ResultAggregator
  with Aggregator
  with ActorLogging {

  import context._

  override def preStart() = {
    for ((k, v) <- actors) printf("key: %s, value: %s\n", k, v)
    //log.debug(s"Starting ${this.toString()}")
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(reason, "Restarting due to [{}] when processing [{}]",
      reason.getMessage, message.getOrElse(""))
  }

  expectOnce {
    case GetCustomerAccountBalances(id, types) ⇒
      new AccountAggregator(sender(), id, actors)
    case _ ⇒
      sender() ! CantUnderstand
      stop(self)
  }
  /*
  override val supervisorStrategy = 
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1.second) {
    case _: ArithmeticException => SupervisorStrategy.Resume
    case _: NullPointerException => SupervisorStrategy.Restart
    case _: IllegalArgumentException => SupervisorStrategy.Stop
    case _: Exception => SupervisorStrategy.Escalate
  }
  */
  class AccountAggregator(originalSender: ActorRef, id: Long, actors: Map[AccountType, Props]) {

    val types = actors.keySet
    initResults[AccountType](types)
    if (types.size > 0)
      types foreach {
        case Checking ⇒ {
          def fWithSender(originalSender: ActorRef)(a: Any) = a match {
            case CheckingAccountBalances(balances) => addResult(0, (Checking -> balances), originalSender)
            case MoneyMarketAccountBalances(balances) ⇒ addResult(2, (MoneyMarket -> balances), originalSender)
          }

          def f = fWithSender(originalSender)(_)
          val pfCloseOverSender = PartialFunction(f)
          val msg = new GetAccountBalances(id)
          fetchAccountsBalance(actors.get(Checking).get, PartialFunction(f), msg, originalSender)
        }
        //case Checking    ⇒ fetchCheckingAccountsBalance(actors.get(Checking).get, context, id, originalSender)
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
  def createProps(actors: Map[AccountType, Props]): Props = {
    Props(classOf[AccountBalanceRetriever], actors)
  }

  case class GetCustomerAccountBalances(id: Long, accountTypes: Set[AccountType])
}
