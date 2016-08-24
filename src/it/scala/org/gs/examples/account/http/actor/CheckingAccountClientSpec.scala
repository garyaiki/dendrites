/** Copyright 2016 Gary Struthers

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.gs.examples.account.http.actor

import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{Matchers, WordSpecLike}
import scala.math.BigDecimal.double2bigDecimal
import org.gs.examples.account.{CheckingAccountBalances, GetAccountBalances}

/**
  *
  * @author Gary Struthers
  */
class CheckingAccountClientSpec extends TestKit(ActorSystem("test")) with ImplicitSender
          with WordSpecLike {

  val clientConfig = CheckingAccountClient.clientConfig

  "A CheckingAccountClient" should {
    "get balances when id 1 exists" in {
      val client = system.actorOf(CheckingAccountClient.props(clientConfig), "Checking")

      client ! GetAccountBalances(1L)
      val obj = expectMsg(Right(CheckingAccountBalances[BigDecimal](Some(List((1, 1000.1))))))
    }
  }

  it should {
    "get balances when id 2 exists" in {
      val client = system.actorOf(CheckingAccountClient.props(clientConfig), "Checking2")

      client ! GetAccountBalances(2L)
      val obj = expectMsg(Right(CheckingAccountBalances(Some(List((2L, BigDecimal(2000.20)),
        (22L, BigDecimal(2200.22)))))))
    }
  }

  it should {
    "get balances when id 3 exists" in {
      val client = system.actorOf(CheckingAccountClient.props(clientConfig), "Checking3")

      client ! GetAccountBalances(3L)
      val obj = expectMsg(Right(CheckingAccountBalances(Some(List((3L, BigDecimal(3000.30)),
        (33L, BigDecimal(3300.33)),
        (333L, BigDecimal(3330.33)))))))
    }
  }

  it should {
    "fail get balances when id 4 doesn't exist" in {
      val client = system.actorOf(CheckingAccountClient.props(clientConfig), "Checking4")

      client ! GetAccountBalances(4L)
      val obj = expectMsg(Left("Checking account 4 not found"))
    }
  }
}
