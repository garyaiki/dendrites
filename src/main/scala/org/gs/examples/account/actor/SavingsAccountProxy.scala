package org.gs.examples.account.actor

import akka.actor.{ Actor, ActorContext, ActorLogging }
import akka.actor._
import akka.contrib.pattern.Aggregator
import org.gs.aggregator.actor.ResultAggregator
import org.gs.examples.account.{ GetAccountBalances, Savings, SavingsAccountBalances }
import SavingsAccountProxy._

class SavingsAccountProxy extends Actor with ActorLogging {
  /*
  override def preStart() = {
    log.debug(s"Starting ${this.toString()}")
  }
  */
  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(reason, "Restarting due to [{}] when processing [{}]",
      reason.getMessage, message.getOrElse(""))
  }
  
  def receive = {
    case GetAccountBalances(id: Long) ⇒
      sender() ! SavingsAccountBalances(Some(List((1, 150000), (2, 29000))))
  }
}

object SavingsAccountProxy {
  def props = Props[SavingsAccountProxy]
}
