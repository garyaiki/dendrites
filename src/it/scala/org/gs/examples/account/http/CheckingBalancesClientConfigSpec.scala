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
package org.gs.examples.account.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.math.BigDecimal.double2bigDecimal
import scala.concurrent.ExecutionContext
import org.gs.examples.account.{CheckingAccountBalances, GetAccountBalances}
import org.gs.http.{caseClassToGetQuery, typedQuery, typedFutureResponse}

/**
  *
  * @author Gary Struthers
  */
class CheckingBalancesClientConfigSpec extends WordSpecLike with Matchers with BalancesProtocols {
  implicit val system = ActorSystem("dendrites")
  override implicit val mat = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val ec: ExecutionContext = system.dispatcher
  val clientConfig = new CheckingBalancesClientConfig()
  val hostConfig = clientConfig.hostConfig
  val config = hostConfig._1
  val baseURL = clientConfig.baseURL
  val badBaseURL = baseURL.dropRight(1)

  val timeout = Timeout(3000 millis)

  "A CheckingBalancesClient" should {
    "get balances for id 1" in {
      val id = 1L
      val cc = GetAccountBalances(id)
      val callFuture = typedQuery(baseURL, cc.productPrefix, caseClassToGetQuery)(cc)
      val responseFuture = typedFutureResponse(mapPlain, mapChecking)(callFuture)

      whenReady(responseFuture, timeout) { result =>
        result should equal(Right(CheckingAccountBalances[BigDecimal](Some(List((1, 1000.1))))))
      }
    }
  }

  it should {
    "get balances for id 2" in {
      val id = 2L
      val cc = GetAccountBalances(id)
      val callFuture = typedQuery(baseURL, cc.productPrefix, caseClassToGetQuery)(cc)
      val responseFuture = typedFutureResponse(mapPlain, mapChecking)(callFuture)

      whenReady(responseFuture, timeout) { result =>
        result should equal(Right(CheckingAccountBalances(Some(List((2L, BigDecimal(2000.20)),
          (22L, BigDecimal(2200.22)))))))
      }
    }
  }

  it should {
    "get balances for id 3" in {
      val id = 3L
      val cc = GetAccountBalances(id)
      val callFuture = typedQuery(baseURL, cc.productPrefix, caseClassToGetQuery)(cc)
      val responseFuture = typedFutureResponse(mapPlain, mapChecking)(callFuture)

      whenReady(responseFuture, timeout) { result =>
        result should equal(Right(CheckingAccountBalances(Some(List((3L, BigDecimal(3000.30)),
          (33L, BigDecimal(3300.33)),
          (333L, BigDecimal(3330.33)))))))
      }
    }
  }

  it should {
    "not find bad ids" in {
      val id = 4L
      val cc = GetAccountBalances(id)
      val callFuture = typedQuery(baseURL, cc.productPrefix, caseClassToGetQuery)(cc)
      val responseFuture = typedFutureResponse(mapPlain, mapChecking)(callFuture)

      whenReady(responseFuture, timeout) { result =>
        result should equal(Left("Checking account 4 not found"))
      }
    }
  }

  it should {
    "fail bad request URLs" in {
      val id = 1L
      val cc = GetAccountBalances(id)
      val callFuture = typedQuery(badBaseURL, cc.productPrefix, caseClassToGetQuery)(cc)
      val responseFuture = typedFutureResponse(mapPlain, mapChecking)(callFuture)

      whenReady(responseFuture, timeout) { result =>
        result should equal(Left(
          "FAIL 404 Not Found The requested resource could not be found."))
      }
    }
  }
}
