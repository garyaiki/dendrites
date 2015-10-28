package org.gs.examples.account.http

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import java.util.concurrent.Executors
import org.gs.http.ClientConnectionPool
import org.gs.examples.account.{
  AccountType,
  Checking,
  CheckingAccountBalances,
  GetAccountBalances,
  MoneyMarketAccountBalances,
  SavingsAccountBalances
}
import org.gs.http._
import org.gs.testdriven.StopSystemAfterAll
import org.scalatest.{ Matchers, WordSpecLike }
import org.scalatest._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.util.{ Failure, Success, Try }

class CheckingBalancesClientConfigSpec extends WordSpecLike with Matchers with BalancesProtocols {
  implicit val system = ActorSystem("akka-aggregator")
  override implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val executor = Executors.newSingleThreadExecutor()
  val clientConfig = new CheckingBalancesClientConfig()
  val hostConfig = clientConfig.hostConfig
  val config = hostConfig._1
  val baseURL = clientConfig.baseURL
  val badBaseURL = baseURL.dropRight(1)

  val timeout = Timeout(3000 millis)

  "A CheckingBalancesClient" should {
    "get balances for id 1" in {
      val id = 1L
      val callFuture = typedQuery(GetAccountBalances(id), baseURL)
      val responseFuture = typedResponse(callFuture, mapPlain, mapChecking)

      whenReady(responseFuture, timeout) { result =>
        result should equal(Right(CheckingAccountBalances(Some(List((1, 1000.1))))))
      }
    }
  }

  it should {
    "get balances for id 2" in {
      val id = 2L
      val callFuture = typedQuery(GetAccountBalances(id), baseURL)
      val responseFuture = typedResponse(callFuture, mapPlain, mapChecking)
      whenReady(responseFuture, timeout) { result =>
        result should equal(Right(CheckingAccountBalances(Some(List((2L, BigDecimal(2000.20)),
          (22L, BigDecimal(2200.22)))))))
      }
    }
  }

  it should {
    "get balances for id 3" in {
      val id = 3L
      val callFuture = typedQuery(GetAccountBalances(id), baseURL)
      val responseFuture = typedResponse(callFuture, mapPlain, mapChecking)
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
      val callFuture = typedQuery(GetAccountBalances(id), baseURL)
      val responseFuture = typedResponse(callFuture, mapPlain, mapChecking)
      whenReady(responseFuture, timeout) { result =>
        result should equal(Left("Checking account 4 not found"))
      }
    }
  }

  it should {
    "fail bad request URLs" in {
      val id = 1L
      val callFuture = typedQuery(GetAccountBalances(id), badBaseURL)
      val responseFuture = typedResponse(callFuture, mapPlain, mapChecking)
      whenReady(responseFuture, timeout) { result =>
        result should equal(Left(
          "FAIL 404 Not Found The requested resource could not be found."))
      }
    }
  }
}