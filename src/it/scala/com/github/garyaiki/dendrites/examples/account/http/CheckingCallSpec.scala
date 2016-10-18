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
package com.github.garyaiki.dendrites.examples.account.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer

import java.util.concurrent.Executors
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}

import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._

import com.github.garyaiki.dendrites.examples.account.http.CheckingBalancesClientConfig;

import scala.math.BigDecimal.double2bigDecimal
import scala.concurrent.ExecutionContext
import com.github.garyaiki.dendrites.examples.account.{CheckingAccountBalances, GetAccountBalances}
import com.github.garyaiki.dendrites.http.{caseClassToGetQuery, typedQueryResponse}

/**
  *
  * @author Gary Struthers
  */
class CheckingCallSpec extends WordSpecLike with Matchers with BeforeAndAfter
        with BalancesProtocols {
  implicit val system = ActorSystem("dendrites")
  implicit val ec: ExecutionContext = system.dispatcher
  override implicit val mat = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val clientConfig = new CheckingBalancesClientConfig()
  val hostConfig = clientConfig.hostConfig
  val config = hostConfig._1
  val baseURL = clientConfig.baseURL
  val badBaseURL = baseURL.dropRight(1)
  val timeout = Timeout(3000 millis)
  def partial = typedQueryResponse(
          baseURL, "GetAccountBalances", caseClassToGetQuery, mapPlain, mapChecking) _
  def badPartial = typedQueryResponse(
          badBaseURL, "GetAccountBalances", caseClassToGetQuery, mapPlain, mapChecking) _

  before {
      val id = 1L 
      val responseFuture = partial(GetAccountBalances(id))

      whenReady(responseFuture, Timeout(120000 millis)) { result => }    
  }

  "A CheckingCallClient" should {
    "get balances for id 1" in {
      val id = 1L 
      val responseFuture = partial(GetAccountBalances(id))

      whenReady(responseFuture, timeout) { result =>
        result shouldBe Right(CheckingAccountBalances[BigDecimal](Some(List((1, 1000.1)))))
      }
    }
  }

  it should {
    "get balances for id 2" in {
      val id = 2L
      val responseFuture = partial(GetAccountBalances(id))
      
      whenReady(responseFuture, timeout) { result =>
        result shouldBe Right(CheckingAccountBalances(Some(List((2L, BigDecimal(2000.20)),
          (22L, BigDecimal(2200.22))))))
      }
    }
  }

  it should {
    "get balances for id 3" in {
      val id = 3L
      val responseFuture = partial(GetAccountBalances(id))
      
      whenReady(responseFuture, timeout) { result =>
        result shouldBe Right(CheckingAccountBalances(Some(List((3L, BigDecimal(3000.30)),
          (33L, BigDecimal(3300.33)), (333L, BigDecimal(3330.33))))))
      }
    }
  }

  it should {
    "not find bad ids" in {
      val id = 4L
      val responseFuture = partial(GetAccountBalances(id))
      
      whenReady(responseFuture, timeout) { result =>
        result shouldBe Left("Checking account 4 not found")
      }
    }
  }

  it should {
    "fail bad request URLs" in {
      val id = 1L
      val responseFuture = badPartial(GetAccountBalances(id))
      
      whenReady(responseFuture, timeout) { result =>
        result shouldBe Left("FAIL 404 Not Found The requested resource could not be found.")
      }
    }
  }
}
