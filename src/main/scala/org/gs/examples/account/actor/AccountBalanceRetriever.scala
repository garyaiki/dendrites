package org.gs.examples.account.actor

import akka.actor._
import akka.contrib.pattern.Aggregator
import org.gs.aggregator.actor.{ CantUnderstand, ResultAggregator, TimedOut }
import org.gs.examples.account._
import org.gs.examples.account.actor.AccountBalanceRetriever._
import scala.collection.immutable.Set
import scala.concurrent.duration._

/** Sample and test code for the aggregator patter.
  * This is based on Jamie Allen's tutorial at
  * http://jaxenter.com/tutorial-asynchronous-programming-with-akka-actors-46220.html
  */

class AccountBalanceRetriever(actors: Map[AccountType, Props]) extends AccountBalanceActor
  with AccountPartialFunctions {

  import context._
/*
  override def preStart() = {
    for ((k, v) <- actors) log.debug(s"key:$k, value:$v")
  }
*/
  expectOnce {
    case GetCustomerAccountBalances(id, types) ⇒
      new AccountAggregator(sender(), id, actors)
    case _ ⇒
      sender() ! CantUnderstand
      stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1.second) {
      case _: ArithmeticException      => SupervisorStrategy.Resume
      case _: NullPointerException     => SupervisorStrategy.Restart
      case _: IllegalArgumentException => SupervisorStrategy.Stop
      case _: Exception                => SupervisorStrategy.Escalate
    }
}

object AccountBalanceRetriever {
  def createProps(actors: Map[AccountType, Props]): Props = {
    Props(classOf[AccountBalanceRetriever], actors)
  }
}
