package org.gs.examples.account.http

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.http.scaladsl.unmarshalling.Unmarshaller.UnsupportedContentTypeException
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
import matchers.ShouldMatchers._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._

class SavingsBalancesClientConfigSpec extends WordSpecLike with Matchers with BalancesProtocols {
  implicit val system = ActorSystem("dendrites")
  override implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val executor = Executors.newSingleThreadExecutor()
  val clientConfig = new SavingsBalancesClientConfig()
  val hostConfig = clientConfig.hostConfig
  val config = hostConfig._1
  val flow = ClientConnectionPool(hostConfig._2, hostConfig._3)
  val baseURL = clientConfig.baseURL
  val badBaseURL = baseURL.dropRight(1)
  val timeout = Timeout(3000 millis)

  "A SavingsBalancesClient" should {
    "get balances for id 1" in {
      val id = 1L
      val callFuture = typedQuery(GetAccountBalances(id), baseURL)
      val responseFuture = typedResponse(callFuture, mapPlain, mapSavings)
      whenReady(responseFuture, timeout) { result =>
        result should equal(Right(SavingsAccountBalances[BigDecimal](Some(List((1, 111000.1))))))
      }
    }
  }

  it should {
    "get balances for id 2" in {
      val id = 2L
      val callFuture = typedQuery(GetAccountBalances(id), baseURL)
      val responseFuture = typedResponse(callFuture, mapPlain, mapSavings)
      whenReady(responseFuture, timeout) { result =>
        result should equal(Right(SavingsAccountBalances(Some(List((2L, BigDecimal(222000.20)),
          (22L, BigDecimal(222200.22)))))))
      }
    }
  }

  it should {
    "get balances for id 3" in {
      val id = 3L
      val callFuture = typedQuery(GetAccountBalances(id), baseURL)
      val responseFuture = typedResponse(callFuture, mapPlain, mapSavings)
      whenReady(responseFuture, timeout) { result =>
        result should equal(Right(SavingsAccountBalances(Some(List((3L, BigDecimal(333000.30)),
          (33L, BigDecimal(333300.33)),
          (333L, BigDecimal(333330.33)))))))
      }
    }
  }

  it should {
    "not find bad ids" in {
      val id = 4L
      val callFuture = typedQuery(GetAccountBalances(id), baseURL)
      val responseFuture = typedResponse(callFuture, mapPlain, mapSavings)
      whenReady(responseFuture, timeout) { result =>
        result should equal(Left("Savings account 4 not found"))
      }
    }
  }

  it should {
    "fail bad request URLs" in {
      val id = 1L
      val callFuture = typedQuery(GetAccountBalances(id), badBaseURL)
      val responseFuture = typedResponse(callFuture, mapPlain, mapSavings)
      whenReady(responseFuture, timeout) { result =>
        result should equal(Left(
          "FAIL 404 Not Found The requested resource could not be found."))
      }
    }
  }
}