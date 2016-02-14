/**
  */
package org.gs.examples.account.http.actor

import akka.actor._
import akka.testkit.{ ImplicitSender, TestKit }
import org.gs._
import org.gs.examples.account._
import org.gs.examples.account.http.actor.AccountBalanceRestfulRetriever._
import org.scalatest.FunSuiteLike
import org.scalatest.Matchers
import scala.concurrent.duration._
import scala.reflect.runtime.universe._
import org.gs.reflection._
/** Sample and test code for the aggregator patter.
  * This is based on Jamie Allen's tutorial at
  * http://jaxenter.com/tutorial-asynchronous-programming-with-akka-actors-46220.html
  */

//#demo-code

//#chain-sample

class AccountBalanceRestfulRetrieverSpec extends TestKit(ActorSystem("test")) with ImplicitSender with FunSuiteLike with Matchers {
  val clientConfig = CheckingAccountClient.clientConfig
  val mmClientConfig = MoneyMarketAccountClient.clientConfig
  val sClientConfig = SavingsAccountClient.clientConfig

  test("Test request 1 account type") {
    val actors = Map(Checking -> CheckingAccountClient.props(clientConfig),
      MoneyMarket -> MoneyMarketAccountClient.props(mmClientConfig),
      Savings -> SavingsAccountClient.props(sClientConfig))
    val props = Props(classOf[AccountBalanceRestfulRetriever], actors)

    system.actorOf(props) ! GetCustomerAccountBalances(1, Set(Savings))
    receiveOne(3.seconds) match {
      case result: IndexedSeq[Product] ⇒ {
        assert(result(1) === (Savings, Some(List((1, 111000.1)))))
      }
      case result ⇒ assert(false, s"Expect 1 AccountType, got $result")
    }
  }

  test("Test request 3 account types") {
    val actors = Map(Checking -> CheckingAccountClient.props(clientConfig),
      MoneyMarket -> MoneyMarketAccountClient.props(mmClientConfig),
      Savings -> SavingsAccountClient.props(sClientConfig))
    val props = Props(classOf[AccountBalanceRestfulRetriever], actors)
    system.actorOf(props) !
      GetCustomerAccountBalances(2, Set(Checking, Savings, MoneyMarket))
    receiveOne(10.seconds) match {
      case result: IndexedSeq[Product] ⇒ {
        assert(result(0) === (Checking, Some(List((2, 2000.2), (22, 2200.22)))))
        assert(result(1) === (Savings, Some(List((2, 222000.2), (22, 222200.22)))))
        assert(result(2) === (MoneyMarket, Some(List((2L, 22000.20), (22L, 22200.22)))))
      }
      case result ⇒ assert(false, s"Expect 3 AccountTypes, got $result")
    }
    //system.shutdown()
  }
}

