/**
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package org.gs.akka.algebird.semigroup.ordered
import akka.contrib.pattern.Aggregator
import akka.testkit.{ ImplicitSender, TestKit }
import org.scalatest.FunSuiteLike
import org.scalatest.Matchers

//#demo-code
import scala.collection._
import scala.concurrent.duration._
import scala.math.BigDecimal.int2bigDecimal

import akka.actor._
/**
 * Sample and test code for the aggregator patter.
 * This is based on Jamie Allen's tutorial at
 * http://jaxenter.com/tutorial-asynchronous-programming-with-akka-actors-46220.html
 */

//#demo-code


//#chain-sample

class MaxSpec extends TestKit(ActorSystem("test")) with ImplicitSender with FunSuiteLike with Matchers {

  test("Test request 2 services") {
    system.actorOf(Props[MaxRetriever[Int]]) !
      GetCustomerAccountBalances(1, 2)
    receiveOne(10.seconds) match {
      case result: List[_] ⇒
        result should have size 2
      case result ⇒
        assert(false, s"Expect List, got ${result.getClass}")
    }
    system.shutdown()
  }
}

