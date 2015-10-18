package org.gs.examples.account.http.actor

import akka.actor._
import akka.testkit.{ ImplicitSender, TestActorRef, TestKit }
import org.gs.examples.account.{ Savings, SavingsAccountBalances, GetAccountBalances }
import org.gs.testdriven.StopSystemAfterAll
import org.scalatest.{MustMatchers,WordSpecLike}

class SavingsAccountClientSpec extends TestKit(ActorSystem("test"))
  with ImplicitSender
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {

  val clientConfig = SavingsAccountClient.clientConfig

  "A SavingsAccountClient" should {
    "get balances when id 1 exists" in {
      val client = system.actorOf(SavingsAccountClient.props(clientConfig), "Savings")
      client ! GetAccountBalances(1L)
      val obj = expectMsg(Right(SavingsAccountBalances(Some(List((1, 111000.1))))))
    }
  }

  it should {
    "get balances when id 2 exists" in {
      val client = system.actorOf(SavingsAccountClient.props(clientConfig), "Savings2")

      client ! GetAccountBalances(2L)
      val obj = expectMsg(Right(SavingsAccountBalances(Some(List((2L, BigDecimal(222000.20)),
                                                                  (22L, BigDecimal(222200.22)))))))
    }
  }

  it should {
    "get balances when id 3 exists" in {
      val client = system.actorOf(SavingsAccountClient.props(clientConfig), "Savings3")

      client ! GetAccountBalances(3L)
      val obj = expectMsg(Right(SavingsAccountBalances(Some(List((3L, BigDecimal(333000.30)),
                                                                  (33L, BigDecimal(333300.33)),
                                                                  (333L, BigDecimal(333330.33)))))))
    }
  }

  it should {
    "fail get balances when id 4 doesn't exist" in {
      val client = system.actorOf(SavingsAccountClient.props(clientConfig), "Savings4")

      client ! GetAccountBalances(4L)
            val obj = expectMsg(Left("Savings account 4 not found"))
    }
  }
}