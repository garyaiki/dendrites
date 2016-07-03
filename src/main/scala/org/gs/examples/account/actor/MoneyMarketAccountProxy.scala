package org.gs.examples.account.actor

import akka.actor.{Actor, ActorContext, ActorLogging, Props}
import akka.contrib.pattern.Aggregator
import org.gs.aggregator.actor.ResultAggregator
import org.gs.examples.account.{GetAccountBalances, MoneyMarket, MoneyMarketAccountBalances}

class MoneyMarketAccountProxy extends Actor with ActorLogging {
  /*
  override def preStart() = {
    log.debug(s"Starting ${this.toString()}")
  }
  */
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "Restarting due to [{}] when processing [{}]",
        reason.getMessage, message.getOrElse(""))
  }

  def receive = {
    case GetAccountBalances(id: Long) ⇒
      sender() ! MoneyMarketAccountBalances(None)
  }
}

object MoneyMarketAccountProxy {
  def props: Props = Props[MoneyMarketAccountProxy]
}
