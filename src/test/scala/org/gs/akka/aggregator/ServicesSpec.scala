/**
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package org.gs.akka.aggregator
import akka.contrib.pattern.Aggregator
import akka.testkit.{ ImplicitSender, TestKit }
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

//#demo-code
import scala.collection._
import scala.concurrent.duration._
import scala.math.BigDecimal.int2bigDecimal
import scala.util.Failure
import scala.language.postfixOps

import akka.actor._
import org.gs.akka.aggregator.examples._
/**
 * Sample and test code for the aggregator patter.
 */

class ServicesSpec extends TestKit(ActorSystem("test")) with ImplicitSender with WordSpecLike
  with Matchers with BeforeAndAfterAll {

  override def afterAll {
    shutdown()
  }

  "A ServicesRetrieverActor" when {
    "all responses succeed" should {
      "Respond with the same number of messages it sent" in {
        system.actorOf(Props[ServicesRetriever[Long]]) ! TellServices(1, proxies, 0)
        receiveOne(10 seconds) match {
          case result: List[_] ⇒
            result should have size 2
            result should contain(exampleResult1)
            result should contain(exampleResult2)
          case result ⇒ assert(false, s"Expect List, got ${result.getClass}")
        }
      }
    }
    "any response fails" should {
      "return a Failure wrapping the exception" in {
        system.actorOf(Props[ServicesRetriever[Long]]) ! TellServices(1, badProxies, 0)
        receiveOne(10 seconds) match {
          case Failure(e) ⇒ e shouldBe exampleError
          case result ⇒ assert(false, s"Expect List, got ${result.getClass}")
        }
      }
    }
  }
}
