package org.gs.examples.account.akka

import akka.actor.{ Actor, ActorContext }
import akka.actor._
import akka.contrib.pattern.Aggregator
import org.gs.akka.aggregator.ResultAggregator
import org.gs.examples.account.{ GetAccountBalances, MoneyMarket, MoneyMarketAccountBalances }
import MoneyMarketAccountProxy._

class MoneyMarketAccountProxy extends Actor {
  def receive = {
    case GetAccountBalances(id: Long) ⇒
      sender() ! MoneyMarketAccountBalances(None)
  }
}

object MoneyMarketAccountProxy {
  def props = Props[MoneyMarketAccountProxy]
}

trait MoneyMarketAccountFetcher {
  this: Actor with ResultAggregator with Aggregator ⇒

  def fetchMoneyMarketAccountsBalance(context: ActorContext, id: Long, recipient: ActorRef) {
    context.actorOf(Props[MoneyMarketAccountProxy]) ! GetAccountBalances(id)
    expectOnce {
      case MoneyMarketAccountBalances(balances) ⇒
        addResult(2, (MoneyMarket -> balances), recipient)
    }
  }
}