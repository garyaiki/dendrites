package org.gs.examples.account.http.actor

import akka.actor._
import akka.testkit.{ ImplicitSender, TestActorRef, TestKit }
import org.gs.examples.account.{ AccountType, MoneyMarket, MoneyMarketAccountBalances, GetAccountBalances }
import org.gs.testdriven.StopSystemAfterAll
import org.scalatest.{MustMatchers,WordSpecLike}

class MoneyMarketAccountClientSpec extends TestKit(ActorSystem("test"))
  with ImplicitSender
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {

  val clientConfig = MoneyMarketAccountClient.clientConfig

  "A MoneyMarketAccountClient" should {
    "get balances when id 1 exists" in {
      val client = system.actorOf(MoneyMarketAccountClient.props(clientConfig), "MoneyMarket")
      client ! GetAccountBalances(1L)
      val obj = expectMsg(Right(MoneyMarketAccountBalances(Some(List((1, 11000.1))))))
    }
  }

  it should {
    "get balances when id 2 exists" in {
      val client = system.actorOf(MoneyMarketAccountClient.props(clientConfig), "MoneyMarket2")

      client ! GetAccountBalances(2L)
      val obj = expectMsg(Right(MoneyMarketAccountBalances(Some(List((2L, BigDecimal(22000.20)),
                                                                  (22L, BigDecimal(22200.22)))))))
    }
  }

  it should {
    "get balances when id 3 exists" in {
      val client = system.actorOf(MoneyMarketAccountClient.props(clientConfig), "MoneyMarket3")

      client ! GetAccountBalances(3L)
      val obj = expectMsg(Right(MoneyMarketAccountBalances(Some(List((3L, BigDecimal(33000.30)),
                                                                  (33L, BigDecimal(33300.33)),
                                                                  (333L, BigDecimal(33330.33)))))))
    }
  }

  it should {
    "fail get balances when id 4 doesn't exist" in {
      val client = system.actorOf(MoneyMarketAccountClient.props(clientConfig), "MoneyMarket4")

      client ! GetAccountBalances(4L)
            val obj = expectMsg(Left("Money Market account 4 not found"))
    }
  }
}