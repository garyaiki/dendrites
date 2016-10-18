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
import akka.stream.{ActorMaterializer, Supervision}
import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.scaladsl.{Flow, Keep}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}

import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._

import com.github.garyaiki.dendrites.examples.account.http.stream.CheckingCallFlow;

import scala.concurrent.ExecutionContext
import scala.math.BigDecimal.double2bigDecimal
import com.github.garyaiki.dendrites.examples.account.{CheckingAccountBalances, GetAccountBalances}
import com.github.garyaiki.dendrites.examples.account.http.{BalancesProtocols, CheckingBalancesClientConfig}
import com.github.garyaiki.dendrites.http.{caseClassToGetQuery, typedQueryResponse}

/**
  *
  * @author Gary Struthers
  */
class CheckingCallFlowSpec extends WordSpecLike with Matchers with BeforeAndAfter
        with BalancesProtocols {
  implicit val system = ActorSystem("dendrites")
  implicit val ec: ExecutionContext = system.dispatcher
  override implicit val mat = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val timeout = Timeout(200 millis)
  def source = TestSource.probe[Product]
  def sink = TestSink.probe[Either[String, AnyRef]]
  val ccf = new CheckingCallFlow
  val testFlow = source.via(ccf.flow).toMat(sink)(Keep.both)

  before { // init connection pool 
    val id = 1L
    val clientConfig = new CheckingBalancesClientConfig()
    val baseURL = clientConfig.baseURL
    def partial = typedQueryResponse(
            baseURL, "GetAccountBalances", caseClassToGetQuery, mapPlain, mapChecking) _

    val responseFuture = partial(GetAccountBalances(id))

    whenReady(responseFuture, Timeout(120000 millis)) { result => }    
  }

  "A CheckingCallFlowClient" should {
    "get balances for id 1" in {
      val id = 1L
      val (pub, sub) = testFlow.run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      response should equal(Right(CheckingAccountBalances[BigDecimal](Some(List((1, 1000.1))))))
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
      
      response should equal(Right(CheckingAccountBalances(Some(List((2L, BigDecimal(2000.20)),
          (22L, BigDecimal(2200.22)))))))
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
      
      response should equal(Right(CheckingAccountBalances(Some(List((3L, BigDecimal(3000.30)),
          (33L, BigDecimal(3300.33)),
          (333L, BigDecimal(3330.33)))))))
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
      
      response should equal(Left("Checking account 4 not found"))
    }
  }
  
  val clientConfig = new CheckingBalancesClientConfig()
  val badBaseURL = clientConfig.baseURL.dropRight(1)

  def badPartial = typedQueryResponse(
          badBaseURL, "GetAccountBalances", caseClassToGetQuery, mapPlain, mapChecking) _

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
