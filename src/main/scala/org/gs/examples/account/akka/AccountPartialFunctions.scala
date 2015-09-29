package org.gs.examples.account.akka

import akka.actor.{ Actor, ActorContext, ActorLogging, ActorRef }
import akka.contrib.pattern.Aggregator
import org.gs.akka.aggregator.ResultAggregator
import org.gs.examples.account.{ Checking, CheckingAccountBalances, MoneyMarket,
                  MoneyMarketAccountBalances, Savings, SavingsAccountBalances,  GetAccountBalances }

trait AccountPartialFunctions {
  this: Actor with ResultAggregator with Aggregator ⇒
  def pfPlusSender(originalSender: ActorRef)(a: Any) = a match {
    case CheckingAccountBalances(balances) =>
        addResult(0, (Checking -> balances), originalSender)
    case MoneyMarketAccountBalances(balances) ⇒
        addResult(2, (MoneyMarket -> balances), originalSender)
    case SavingsAccountBalances(balances) ⇒
        addResult(1, (Savings -> balances), originalSender)
  }
}
