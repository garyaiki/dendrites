package org.gs.examples.account.httpakka

import akka.actor._
import akka.testkit.{ ImplicitSender, TestKit }
import org.scalatest.WordSpecLike
import org.scalatest.MustMatchers
import org.gs.testdriven.StopSystemAfterAll
import scala.collection.mutable.ArrayBuffer
import org.gs.examples.account.AccountType
import org.gs.examples.account.Checking
import akka.testkit.TestActorRef
import org.gs.examples.account.{CheckingAccountBalances, GetAccountBalances}
import org.gs.examples.account.httpakka.CheckingAccountClient._

class CheckingAccountClientSpec extends TestKit(ActorSystem("test"))
    with ImplicitSender
    with WordSpecLike
    with MustMatchers
    with StopSystemAfterAll {

  "A CheckingAccountClient" must {
    "get balances" in {

      val client = system.actorOf(CheckingAccountClient.props, "Checking") 
      client ! GetAccountBalances(1L)
      expectMsg(CheckingAccountBalances(Some(List((3, 15000)))))
    }
  }
}