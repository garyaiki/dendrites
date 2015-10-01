package org.gs.examples.account.http.actor

import akka.actor.{ Actor, ActorContext, ActorLogging, ActorRef }
import akka.contrib.pattern.Aggregator
import org.gs.akka.aggregator.ResultAggregator
import org.gs.examples.account.{ Checking, CheckingAccountBalances, MoneyMarket,
                  MoneyMarketAccountBalances, Savings, SavingsAccountBalances,  GetAccountBalances }

trait AccountRestfulPartialFunctions {
  this: Actor with ResultAggregator ⇒
  def pfPlusSender(originalSender: ActorRef)(a: Any) = a match {
    case Right(CheckingAccountBalances(balances)) =>
        addResult(0, (Checking -> balances), originalSender)
    case Right(MoneyMarketAccountBalances(balances)) ⇒
        addResult(2, (MoneyMarket -> balances), originalSender)
    case Right(SavingsAccountBalances(balances)) ⇒
        addResult(1, (Savings -> balances), originalSender)
    case Left(a) => originalSender ! a
    case _ => log.error(s"No match:$a")
  }
}
