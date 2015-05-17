package org.gs.examples.account.akka

import akka.actor.Actor
import akka.actor.ActorContext
import akka.actor._
import akka.contrib.pattern.Aggregator
import scala.collection.mutable.ArrayBuffer
import org.gs.examples.account._
import org.gs.examples.account.akka.AccountBalanceRetriever._

class CheckingAccountProxy extends Actor with Aggregator {
  override def receive = {
    case GetAccountBalances(id: Long) ⇒
      sender() ! CheckingAccountBalances(Some(List((3, 15000))))
  }
}

object CheckingAccountProxy {
  def props = Props[CheckingAccountProxy]
}