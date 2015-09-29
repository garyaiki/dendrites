package org.gs.examples.account.akka

import akka.actor.{Actor, ActorContext, ActorLogging }
import akka.actor._
import akka.contrib.pattern.Aggregator
import org.gs.akka.aggregator.ResultAggregator
import org.gs.examples.account.{Checking, CheckingAccountBalances, GetAccountBalances}
import CheckingAccountProxy._

class CheckingAccountProxy extends Actor with ActorLogging {
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
      sender() ! CheckingAccountBalances(Some(List((3, 15000))))
  }
}

object CheckingAccountProxy {
  def props = Props[CheckingAccountProxy]
}
