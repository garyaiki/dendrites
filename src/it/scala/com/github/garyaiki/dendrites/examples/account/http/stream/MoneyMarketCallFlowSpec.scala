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
package com.github.garyaiki.dendrites.examples.account.http.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Flow}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}

import java.util.concurrent.Executors
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}

import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._

import com.github.garyaiki.dendrites.examples.account.http.stream.MoneyMarketCallFlow;

import scala.concurrent.ExecutionContext
import scala.math.BigDecimal.double2bigDecimal
import com.github.garyaiki.dendrites.examples.account.{GetAccountBalances, MoneyMarketAccountBalances}
import com.github.garyaiki.dendrites.examples.account.http.{BalancesProtocols, MoneyMarketBalancesClientConfig}
import com.github.garyaiki.dendrites.http.{caseClassToGetQuery, typedQueryResponse}

/**
  *
  * @author Gary Struthers
  */
class MoneyMarketCallFlowSpec extends WordSpecLike with Matchers with BeforeAndAfter
    with BalancesProtocols {
  implicit val system = ActorSystem("dendrites")
  implicit val ec: ExecutionContext = system.dispatcher
  override implicit val mat = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val executor = Executors.newSingleThreadExecutor()
  val timeout = Timeout(200 millis)

  def source = TestSource.probe[Product]
  def sink = TestSink.probe[Either[String, AnyRef]]
  val mmcf = new MoneyMarketCallFlow
  val testFlow = source.via(mmcf.flow).toMat(sink)(Keep.both)

  before { // init connection pool 
    val id = 1L
    val clientConfig = new MoneyMarketBalancesClientConfig()
    val baseURL = clientConfig.baseURL
    def partial = typedQueryResponse(
            baseURL, "GetAccountBalances", caseClassToGetQuery, mapPlain, mapMoneyMarket) _
      val responseFuture = partial(GetAccountBalances(id))

      whenReady(responseFuture, Timeout(120000 millis)) { result => }    
  }

  "A MoneyMarketCallFlowClient" should {
    "get balances for id 1" in {
      val id = 1L
      val (pub, sub) = testFlow.run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      response should equal(Right(MoneyMarketAccountBalances[BigDecimal](Some(List((1, 11000.1))))))
    }
  }

  it should {
    "get balances for id 2" in {
      val id = 2L
      val (pub, sub) = testFlow.run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      
      response should equal(Right(MoneyMarketAccountBalances(Some(List((2L, BigDecimal(22000.20)),
          (22L, BigDecimal(22200.22)))))))
    }
  }

  it should {
    "get balances for id 3" in {
      val id = 3L
      val (pub, sub) = testFlow.run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      
      response should equal(Right(MoneyMarketAccountBalances(Some(List((3L, BigDecimal(33000.30)),
          (33L, BigDecimal(33300.33)),
          (333L, BigDecimal(33330.33)))))))
    }
  }

  it should {
    "not find bad ids" in {
      val id = 4L
      val (pub, sub) = testFlow.run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      
      response should equal(Left("Money Market account 4 not found"))
    }
  }

  val clientConfig = new MoneyMarketBalancesClientConfig()
  val badBaseURL = clientConfig.baseURL.dropRight(1)
  def badPartial = typedQueryResponse(
          badBaseURL, "GetAccountBalances", caseClassToGetQuery, mapPlain, mapMoneyMarket) _
  def badFlow: Flow[Product, Either[String, AnyRef], NotUsed] =
          Flow[Product].mapAsync(1)(badPartial)

  it should {
    "fail bad request URLs" in {
      val id = 1L
      val (pub, sub) = source
        .via(badFlow)
        .toMat(sink)(Keep.both).run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()

      response should equal(Left("FAIL 404 Not Found The requested resource could not be found."))
    }
  }
}
