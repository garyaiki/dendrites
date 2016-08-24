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
import scala.concurrent.duration._
import scala.reflect.runtime.universe._
import org.gs.examples.account.{Checking, GetCustomerAccountBalances, MoneyMarket, Savings}

/** Sample and test code for the aggregator patter.
  * This is based on Jamie Allen's tutorial at
  * [[http://jaxenter.com/tutorial-asynchronous-programming-with-akka-actors-46220.html Actor prog]]
  */
class AccountBalanceRestfulRetrieverSpec extends TestKit(ActorSystem("test")) with ImplicitSender
        with WordSpecLike with Matchers {
  val clientConfig = CheckingAccountClient.clientConfig
  val mmClientConfig = MoneyMarketAccountClient.clientConfig
  val sClientConfig = SavingsAccountClient.clientConfig

  "an Account client" should {
      "Get 1 account type" in {
        val actors = Map(Checking -> CheckingAccountClient.props(clientConfig),
        MoneyMarket -> MoneyMarketAccountClient.props(mmClientConfig),
        Savings -> SavingsAccountClient.props(sClientConfig))
        val props = Props(classOf[AccountBalanceRestfulRetriever], actors)

        system.actorOf(props) ! GetCustomerAccountBalances(1, Set(Savings))
        receiveOne(3.seconds) match {
        case result: IndexedSeq[Product] ⇒ {
          result(1) shouldBe (Savings, Some(List((1, 111000.1))))
        }
        case result ⇒ assert(false, s"Expect 1 AccountType, got $result")
        }
    }

      "Get 3 account types" in {
        val actors = Map(Checking -> CheckingAccountClient.props(clientConfig),
        MoneyMarket -> MoneyMarketAccountClient.props(mmClientConfig),
        Savings -> SavingsAccountClient.props(sClientConfig))
        val props = Props(classOf[AccountBalanceRestfulRetriever], actors)

        system.actorOf(props) ! GetCustomerAccountBalances(2, Set(Checking, Savings, MoneyMarket))
                
        receiveOne(10.seconds) match {
          case result: IndexedSeq[Product] ⇒ {
            result(0) shouldBe (Checking, Some(List((2, 2000.2), (22, 2200.22))))
            result(1) shouldBe (Savings, Some(List((2, 222000.2), (22, 222200.22))))
            result(2) shouldBe (MoneyMarket, Some(List((2L, 22000.20), (22L, 22200.22))))
          }
          case result ⇒ assert(false, s"Expect 3 AccountTypes, got $result")
        }
      }
  }
}

