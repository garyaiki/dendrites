/** 
  */
package org.gs.akka.aggregator

import akka.actor._
import akka.contrib.pattern.Aggregator
import akka.testkit.{ ImplicitSender, TestKit }
import org.scalatest.FunSuiteLike
import org.scalatest.Matchers

//#demo-code
import scala.collection._
import scala.concurrent.duration._
import scala.math.BigDecimal.int2bigDecimal
import scala.reflect.runtime.universe._

import org.gs._
/** Sample and test code for the aggregator patter.
  * This is based on Jamie Allen's tutorial at
  * http://jaxenter.com/tutorial-asynchronous-programming-with-akka-actors-46220.html
  */

//#demo-code

//#chain-sample

class AggregatorSpec extends TestKit(ActorSystem("test")) with ImplicitSender with FunSuiteLike with Matchers {

  test("Test request 1 account type") {
    system.actorOf(Props[AccountBalanceRetriever]) ! GetCustomerAccountBalances(1, Set(Savings))
    receiveOne(10.seconds) match {
      case result: IndexedSeq[_] ⇒
        assert(result(1).isInstanceOf[(AccountType, Option[List[(Long, BigDecimal)]])])
      case result ⇒
        assert(false, "Expect 1 AccountType, got $result ${weakParamInfo(result)}")
    }
  }

  test("Test request 3 account types") {
    system.actorOf(Props[AccountBalanceRetriever]) !
      GetCustomerAccountBalances(2, Set(Checking, Savings, MoneyMarket))
    receiveOne(10.seconds) match {
      case result: IndexedSeq[_] ⇒ {
        assert(result(0).isInstanceOf[(AccountType, Option[List[(Long, BigDecimal)]])])
        assert(result(1).isInstanceOf[(AccountType, Option[List[(Long, BigDecimal)]])])
        assert(result(2).isInstanceOf[(AccountType, Option[List[(Long, BigDecimal)]])])
      }
      case result ⇒
        assert(false, s"Expect 3 AccountTypes, got $result ${weakParamInfo(result)}")
    }
    //system.shutdown()
  }
}

