/**
  */
package org.gs.examples.account.akka

import akka.actor._
import akka.contrib.pattern.Aggregator
import akka.testkit.{ ImplicitSender, TestKit }
import org.scalatest.FunSuiteLike
import org.scalatest.Matchers

//#demo-code
//import scala.collection
import scala.concurrent.duration._
import scala.math.BigDecimal.int2bigDecimal
import scala.reflect.runtime.universe._

import org.gs._
import org.gs.examples.account._
import org.gs.examples.account.akka.AccountBalanceRetriever._

import org.gs.reflection._
/** Sample and test code for the aggregator patter.
  * This is based on Jamie Allen's tutorial at
  * http://jaxenter.com/tutorial-asynchronous-programming-with-akka-actors-46220.html
  */

//#demo-code

//#chain-sample

class AccountBalanceRetrieverSpec extends TestKit(ActorSystem("test")) with ImplicitSender with FunSuiteLike with Matchers {

  test("Test request 1 account type") {
    val actors = Map(Checking -> Props[CheckingAccountProxy],
      MoneyMarket -> Props[MoneyMarketAccountProxy], Savings -> Props[SavingsAccountProxy])
    val props = Props(classOf[AccountBalanceRetriever], actors)
    //val s = Set(Savings)
    //val ks: Set[AccountType] = actors.keySet
    system.actorOf(props) ! GetCustomerAccountBalances(1, Set(Savings))
    receiveOne(10.seconds) match {
      case result: IndexedSeq[Product] ⇒
        assert(isElementEqual(result(1), 0, Savings))
      case result ⇒ assert(false, s"Expect 1 AccountType, got $result")
    }
  }

  test("Test request 3 account types") {
    val actors = Map(Checking -> Props[CheckingAccountProxy],
      MoneyMarket -> Props[MoneyMarketAccountProxy], Savings -> Props[SavingsAccountProxy])
    val props = Props(classOf[AccountBalanceRetriever], actors)
    system.actorOf(props) !
      GetCustomerAccountBalances(2, Set(Checking, Savings, MoneyMarket))
    receiveOne(10.seconds) match {
      case result: IndexedSeq[Product] ⇒ {
        assert(isElementEqual(result(0), 0, Checking))
        assert(isElementEqual(result(1), 0, Savings))
        assert(isElementEqual(result(2), 0, MoneyMarket))
      }
      case result ⇒ assert(false, s"Expect 3 AccountTypes, got $result")
    }
    //system.shutdown()
  }
}

