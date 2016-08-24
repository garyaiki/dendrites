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
package org.gs.examples.account.http.stream

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext
import scala.math.BigDecimal.double2bigDecimal
import org.gs.examples.account.{CheckingAccountBalances,
                                GetAccountBalances,
                                MoneyMarketAccountBalances,
                                SavingsAccountBalances}
/**
  *
  * @author Gary Struthers
  */
class ParallelCallFlowSpec extends WordSpecLike with Matchers {
  implicit val system = ActorSystem("dendrites")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val timeout = Timeout(3000 millis)
  
  def source = TestSource.probe[Product]
  def sink = TestSink.probe[(Either[String, AnyRef],Either[String, AnyRef],Either[String, AnyRef])]
  val pcf = new ParallelCallFlow
  val wrappedFlow = pcf.asFlow
  
  "A ParallelCallFlowClient" should {
    "get balances for id 1" in {
      val id = 1L
      val (pub, sub) = source
      .via(wrappedFlow)
      .toMat(sink)(Keep.both).run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      response should equal(Right(CheckingAccountBalances[BigDecimal](Some(List((1,1000.1))))),
                            Right(MoneyMarketAccountBalances[BigDecimal](Some(List((1,11000.1))))),
                            Right(SavingsAccountBalances[BigDecimal](Some(List((1,111000.1))))))
    }
  }

  it should {
    "get balances for id 2" in {
      val id = 2L
      val (pub, sub) = source
      .via(wrappedFlow)
      .toMat(sink)(Keep.both).run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      response should equal(
          Right(CheckingAccountBalances[BigDecimal](Some(List((2,2000.2), (22,2200.22))))),
          Right(MoneyMarketAccountBalances[BigDecimal](Some(List((2,22000.2), (22,22200.22))))),
          Right(SavingsAccountBalances[BigDecimal](Some(List((2,222000.2), (22,222200.22))))))
    }
  }

  it should {
    "get balances for id 3" in {
      val id = 3L
      val (pub, sub) = source
      .via(wrappedFlow)
      .toMat(sink)(Keep.both).run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      response should equal(Right(CheckingAccountBalances[BigDecimal](
                                Some(List((3,3000.3), (33,3300.33), (333,3330.33))))),
                            Right(MoneyMarketAccountBalances[BigDecimal](
                                Some(List((3,33000.3), (33,33300.33), (333,33330.33))))),
                            Right(SavingsAccountBalances[BigDecimal](
                                Some(List((3,333000.3), (33,333300.33), (333,333330.33))))))
    }
  }

  it should {
    "not find bad ids" in {
      val id = 4L
      val (pub, sub) = source
      .via(wrappedFlow)
      .toMat(sink)(Keep.both).run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      response should equal(Left("Checking account 4 not found"),
                            Left("Money Market account 4 not found"),
                            Left("Savings account 4 not found"))
    }
  }
}
