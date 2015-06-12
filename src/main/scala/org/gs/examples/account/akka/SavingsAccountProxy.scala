package org.gs.examples.account.akka

import akka.actor.{ Actor, ActorContext }
import akka.actor._
import akka.contrib.pattern.Aggregator
import org.gs.akka.aggregator.ResultAggregator
import org.gs.examples.account.{GetAccountBalances, Savings, SavingsAccountBalances }
import SavingsAccountProxy._

class SavingsAccountProxy extends Actor {
  def receive = {
    case GetAccountBalances(id: Long) ⇒
      sender() ! SavingsAccountBalances(Some(List((1, 150000), (2, 29000))))
  }
}

object SavingsAccountProxy {
  def props = Props[SavingsAccountProxy]
}

trait SavingsAccountFetcher {
  this: Actor with ResultAggregator with Aggregator ⇒
  def fetchSavingsAccountsBalance(context: ActorContext, id: Long, recipient: ActorRef) {
    context.actorOf(SavingsAccountProxy.props) ! GetAccountBalances(id)
    expectOnce {
      case SavingsAccountBalances(balances) ⇒
        addResult(1, (Savings -> balances), recipient)
    }
  }
}