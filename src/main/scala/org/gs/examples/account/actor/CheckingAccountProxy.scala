package org.gs.examples.account.actor

import akka.actor.{Actor, ActorContext, ActorLogging, Props}
import akka.contrib.pattern.Aggregator
import org.gs.aggregator.actor.ResultAggregator
import org.gs.examples.account.{Checking, CheckingAccountBalances, GetAccountBalances}

class CheckingAccountProxy extends Actor with ActorLogging {
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
      sender() ! CheckingAccountBalances[Int](Some(List((3, 15000))))
  }
}

object CheckingAccountProxy {
  def props: Props = Props[CheckingAccountProxy]
}
