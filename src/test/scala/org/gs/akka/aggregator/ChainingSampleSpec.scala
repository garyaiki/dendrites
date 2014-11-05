/**
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package org.gs.akka.aggregator
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

class ChainingSampleSpec extends TestKit(ActorSystem("test")) with ImplicitSender with FunSuiteLike with Matchers {
  test("Test request 1 account type") {
    system.actorOf(Props[ChainingSample]) ! InitialRequest("InitialRequest")
    receiveOne(100.seconds) match {
      case result: FinalResponse ⇒
        result.qualifiedValues should have size 0  
      case result ⇒ println( s"got ${result.getClass}")
    }
    system.shutdown()    
  }

}

