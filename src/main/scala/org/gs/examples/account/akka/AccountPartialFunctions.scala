package org.gs.examples.account.akka

import akka.actor.{ Actor, ActorLogging, ActorRef }
import org.gs.akka.aggregator.ResultAggregator
import org.gs.examples.account.{ Checking, CheckingAccountBalances, MoneyMarket,
                                 MoneyMarketAccountBalances, Savings, SavingsAccountBalances }

/** "expectOnce" result handler used by CheckingAccountProxy, MoneyMarketAccountProxy, and
 *  SavingsAccountProxy child actors. Before creating actor curry originalSender then transform
 *  pfPlusSender from a regular function to a PartialFunction
 *  {{{
 *  def f = pfPlusSender(originalSender)(_)
 *  val pf = PartialFunction(f)
 *  }}}
 *  
 * @author garystruthers
 *
 */
trait AccountPartialFunctions extends PartialFunctionPlusSender {
  this: Actor with ResultAggregator ⇒
  def pfPlusSender(originalSender: ActorRef)(a: Any) = a match {
    case CheckingAccountBalances(balances) =>
        addResult(0, (Checking -> balances), originalSender)
    case MoneyMarketAccountBalances(balances) ⇒
        addResult(2, (MoneyMarket -> balances), originalSender)
    case SavingsAccountBalances(balances) ⇒
        addResult(1, (Savings -> balances), originalSender)
    case _ => log.error(s"No match:$a")
  }
}
