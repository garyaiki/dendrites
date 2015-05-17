package org.gs.examples.account.akka

import akka.actor.Actor
import org.gs.examples.account._
import org.gs.examples.account.akka.AccountBalanceRetriever._

class MoneyMarketAccountProxy extends Actor {
  def receive = {
    case GetAccountBalances(id: Long) ⇒
      sender() ! MoneyMarketAccountBalances(None)
  }
}