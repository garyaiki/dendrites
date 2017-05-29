/**
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
package com.github.garyaiki.dendrites.http.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import java.util.concurrent.Executors
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.math.BigDecimal.double2bigDecimal
import com.github.garyaiki.dendrites.examples.account.{ CheckingAccountBalances, GetAccountBalances}
import com.github.garyaiki.dendrites.examples.account.http.{BalancesProtocols, CheckingBalancesClientConfig}
import com.github.garyaiki.dendrites.http.{caseClassToGetQuery, typedQueryResponse}

/**
  *
  * @author Gary Struthers
  */
class TypedQueryResponseFlowSpec extends WordSpecLike with Matchers with BalancesProtocols {
  implicit val system = ActorSystem("dendrites")
  override implicit val mat = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val executor = Executors.newSingleThreadExecutor()
  val timeout = Timeout(200 millis)

  def source = TestSource.probe[Product]
  def sink = TestSink.probe[Either[String, AnyRef]]

  val clientConfig = new CheckingBalancesClientConfig()
  val hostConfig = clientConfig.hostConfig
  val baseURL = clientConfig.baseURL
  val requestPath = clientConfig.requestPath
  val queryFlow = new TypedQueryFlow(baseURL, requestPath, caseClassToGetQuery)
  val responseFlow = new TypedResponseFlow(mapPlain, mapChecking)
  val tqr = new TypedQueryResponseFlow(queryFlow, responseFlow)
  val testFlow = source.via(tqr.flow).toMat(sink)(Keep.both)

  "A TypedQueryResponseFlow for Checking balances" should {
    "get balances for id 1" in {
      val id = 1L
      val (pub, sub) = testFlow.run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      response shouldBe Right(CheckingAccountBalances[BigDecimal](Some(List((1, 1000.1)))))
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

      response shouldBe Right(CheckingAccountBalances(Some(List((2L, BigDecimal(2000.20)),
        (22L, BigDecimal(2200.22))))))
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

      response shouldBe Right(CheckingAccountBalances(Some(List((3L, BigDecimal(3000.30)),
        (33L, BigDecimal(3300.33)),
        (333L, BigDecimal(3330.33))))))
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

      response shouldBe Left("Checking account 4 not found")
    }
  }

  val badBaseURL = clientConfig.baseURL.dropRight(1)

  def badPartial = typedQueryResponse(badBaseURL, "GetAccountBalances", caseClassToGetQuery, mapPlain, mapChecking) _

  def badFlow: Flow[Product, Either[String, AnyRef], NotUsed] = Flow[Product].mapAsync(1)(badPartial)

  it should {
    "fail bad request URLs" in {
      val id = 1L
      val (pub, sub) = source.via(badFlow).toMat(sink)(Keep.both).run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()
      response shouldBe Left("FAIL 404 Not Found The requested resource could not be found.")
    }
  }
}
