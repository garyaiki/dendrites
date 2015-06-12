package org.gs.examples.account.akka

import akka.actor.{Actor, ActorContext}
import akka.actor._
import akka.contrib.pattern.Aggregator
import org.gs.akka.aggregator.ResultAggregator
import org.gs.examples.account.{Checking, CheckingAccountBalances, GetAccountBalances}
import CheckingAccountProxy._

class CheckingAccountProxy extends Actor {
  def receive = {
    case GetAccountBalances(id: Long) ⇒
      sender() ! CheckingAccountBalances(Some(List((3, 15000))))
  }
}

object CheckingAccountProxy {
  def props = Props[CheckingAccountProxy]
}

trait CheckingAccountFetcher {
  this: Actor with ResultAggregator with Aggregator ⇒
  
  def fetchCheckingAccountsBalance(context: ActorContext, id: Long, recipient: ActorRef) {
    context.actorOf(CheckingAccountProxy.props) ! GetAccountBalances(id)
    expectOnce {
      case CheckingAccountBalances(balances) ⇒
        addResult(0, (Checking -> balances), recipient)
    }
  }
}