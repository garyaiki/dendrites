package org.gs.examples.account.httpakka

import akka.actor._
import akka.pattern.ask
import akka.testkit.{ ImplicitSender, TestKit }
import org.scalatest.WordSpecLike
import org.scalatest.MustMatchers
import org.gs.testdriven.StopSystemAfterAll
import scala.collection.mutable.ArrayBuffer
import org.gs.examples.account.AccountType
import org.gs.examples.account.Checking
import akka.testkit.TestActorRef
import org.gs.examples.account.{ CheckingAccountBalances, GetAccountBalances }
import org.gs.examples.account.httpakka.CheckingAccountClient._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.SpanSugar._


class CheckingAccountClientSpec extends TestKit(ActorSystem("test"))
  with ImplicitSender
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {

  "A CheckingAccountClient" should {
    "get balances when id 1 exists" in {
      val client = system.actorOf(CheckingAccountClient.props, "Checking")

      client ! GetAccountBalances(1L)
      val obj = expectMsg(Right(CheckingAccountBalances(Some(List((1, 1000.1))))))
    }
  }

  it should {
    "get balances when id 2 exists" in {
      val client = system.actorOf(CheckingAccountClient.props, "Checking2")

      client ! GetAccountBalances(2L)
      val obj = expectMsg(Right(CheckingAccountBalances(Some(List((2L, BigDecimal(2000.20)),
                                                                  (22L, BigDecimal(2200.22)))))))
    }
  }

  it should {
    "get balances when id 3 exists" in {
      val client = system.actorOf(CheckingAccountClient.props, "Checking3")

      client ! GetAccountBalances(3L)
      val obj = expectMsg(Right(CheckingAccountBalances(Some(List((3L, BigDecimal(3000.30)),
                                                                  (33L, BigDecimal(3300.33)),
                                                                  (333L, BigDecimal(3330.33)))))))
    }
  }
/*
  it should {
    "fail get balances when id 4 doesn't exist" in {
      val client = system.actorOf(CheckingAccountClient.props, "Checking4")

      client ! GetAccountBalances(4L)
      val obj = expectMsg(
          Right(
              CheckingAccountBalances(
                  Some(
                      List(
                          (2L, BigDecimal(2000.20)),
                          (22L, BigDecimal(2200.22))
                      )
                  )
              )
          )
      )
    }
  }
*/
}