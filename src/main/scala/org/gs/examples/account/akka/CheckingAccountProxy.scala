package org.gs.examples.account.akka

import akka.actor.Actor
import org.gs.examples.account._

class CheckingAccountProxy extends Actor {
  def receive = {
    case GetAccountBalances(id: Long) ⇒
      sender() ! CheckingAccountBalances(Some(List((3, 15000))))
  }
}