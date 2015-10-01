package org.gs.examples.account.http.actor

import akka.actor._
import akka.contrib.pattern.Aggregator
import org.gs.akka.aggregator.{ CantUnderstand, ResultAggregator, TimedOut }
import org.gs.examples.account._
import org.gs.examples.account.akka.AccountBalanceRetriever._
import scala.collection.immutable.Set
import scala.concurrent.duration._

/** Sample and test code for the aggregator patter.
  * This is based on Jamie Allen's tutorial at
  * http://jaxenter.com/tutorial-asynchronous-programming-with-akka-actors-46220.html
  */

class AccountBalanceRestfulRetriever(actors: Map[AccountType, Props]) extends Actor
  with ResultAggregator
  with AccountRestfulPartialFunctions {

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

  override val supervisorStrategy = 
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1.second) {
    case _: ArithmeticException => SupervisorStrategy.Resume
    case _: NullPointerException => SupervisorStrategy.Restart
    case _: IllegalArgumentException => SupervisorStrategy.Stop
    case _: Exception => SupervisorStrategy.Escalate
  }

  class AccountAggregator(originalSender: ActorRef, id: Long, actors: Map[AccountType, Props]) {
    def f = pfPlusSender(originalSender)(_)
    val pf = PartialFunction(f)
    val msg = new GetAccountBalances(id)
    val types = actors.keySet
    initResults[AccountType](types)
    if (types.size > 0)
      types foreach {
        case Checking ⇒ fetchResult(actors.get(Checking).get, pf, msg, originalSender)
        case Savings  ⇒ fetchResult(actors.get(Savings).get, pf, msg, originalSender)
        case MoneyMarket ⇒ fetchResult(actors.get(MoneyMarket).get, pf, msg, originalSender)
      }
    else collectResults(originalSender)

    system.scheduler.scheduleOnce(1.second, self, TimedOut)
    expect {
      case TimedOut ⇒ collectResults(originalSender, force = true)
    }
  }
}

object AccountBalanceRestfulRetriever {
  def createProps(actors: Map[AccountType, Props]): Props = {
    Props(classOf[AccountBalanceRestfulRetriever], actors)
  }

  case class GetCustomerAccountBalances(id: Long, accountTypes: Set[AccountType])
}
