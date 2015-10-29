package org.gs.examples.account.http.actor

import akka.actor.{ Actor, ActorLogging, ActorRef }
import org.gs.akka.aggregator.ResultAggregator
import org.gs.examples.account.{ Checking, CheckingAccountBalances, MoneyMarket,
                                 MoneyMarketAccountBalances, Savings, SavingsAccountBalances }
import org.gs.akka.aggregator.PartialFunctionPlusSender

/** "expectOnce" result handler used by CheckingAccountClient, MoneyMarketAccountClient, and
 *  SavingsAccountClient child actors. Before creating actor curry originalSender then transform
 *  pfPlusSender from a regular function to a PartialFunction
 *  {{{
 *  def f = pfPlusSender(originalSender)(_)
 *  val pf = PartialFunction(f)
 *  }}}
 *  
 * @author garystruthers
 *
 */
trait AccountRestfulPartialFunctions extends PartialFunctionPlusSender {
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
