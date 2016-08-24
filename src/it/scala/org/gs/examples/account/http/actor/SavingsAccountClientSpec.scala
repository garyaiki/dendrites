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
import org.gs.examples.account.{SavingsAccountBalances, GetAccountBalances}

/**
  *
  * @author Gary Struthers
  */
class SavingsAccountClientSpec extends TestKit(ActorSystem("test")) with ImplicitSender
          with WordSpecLike {

  val clientConfig = SavingsAccountClient.clientConfig

  "A SavingsAccountClient" should {
    "get balances when id 1 exists" in {
      val client = system.actorOf(SavingsAccountClient.props(clientConfig), "Savings")
      client ! GetAccountBalances(1L)
      val obj = expectMsg(Right(SavingsAccountBalances[BigDecimal](Some(List((1, 111000.1))))))
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
