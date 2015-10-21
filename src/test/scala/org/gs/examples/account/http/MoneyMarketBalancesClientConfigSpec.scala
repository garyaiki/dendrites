package org.gs.examples.account.http
import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import java.util.concurrent.Executors
import org.gs.akka.http.ClientConnectionPool
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
import scala.collection.mutable.ArrayBuffer
import scala.util.{ Failure, Success, Try }
import org.gs.examples.account.http.actor.CheckingAccountClient._

class MoneyMarketBalancesClientConfigSpec extends WordSpecLike with Matchers with BalancesProtocols {
  implicit val system = ActorSystem("akka-aggregator")
  override implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val executor = Executors.newSingleThreadExecutor()
  val clientConfig = new MoneyMarketBalancesClientConfig()
  val hostConfig = clientConfig.hostConfig
  val config = hostConfig._1
  //val flow = ClientConnectionPool(hostConfig._2, hostConfig._3)
  val baseURL = clientConfig.baseURL
  val badBaseURL = baseURL.dropRight(1)

  val timeout = Timeout(3000 millis)

  "A MoneyBalancesClient" should {
    "get balances for id 1" in {
      val id = 1L
      val callFuture = typedQuery(GetAccountBalances(id), baseURL)
      val responseFuture = typedResponse(callFuture, mapPlain, mapMoneyMarket)

      whenReady(responseFuture, timeout) { result =>
        result should equal(Right(MoneyMarketAccountBalances(Some(List((1, 11000.1))))))
      }
    }
  }

  it should {
    "get balances for id 2" in {
      val id = 2L
      val callFuture = typedQuery(GetAccountBalances(id), baseURL)
      val responseFuture = typedResponse(callFuture, mapPlain, mapMoneyMarket)
      whenReady(responseFuture, timeout) { result =>
        result should equal(Right(MoneyMarketAccountBalances(Some(List((2L, BigDecimal(22000.20)),
          (22L, BigDecimal(22200.22)))))))
      }
    }
  }

  it should {
    "get balances for id 3" in {
      val id = 3L
      val callFuture = typedQuery(GetAccountBalances(id), baseURL)
      val responseFuture = typedResponse(callFuture, mapPlain, mapMoneyMarket)
      whenReady(responseFuture, timeout) { result =>
        result should equal(Right(MoneyMarketAccountBalances(Some(List((3L, BigDecimal(33000.30)),
          (33L, BigDecimal(33300.33)),
          (333L, BigDecimal(33330.33)))))))
      }
    }
  }

  it should {
    "not find bad ids" in {
      val id = 4L
      val callFuture = typedQuery(GetAccountBalances(id), baseURL)
      val responseFuture = typedResponse(callFuture, mapPlain, mapMoneyMarket)
      whenReady(responseFuture, timeout) { result =>
        result should equal(Left("Money Market account 4 not found"))
      }
    }
  }

  it should {
    "fail bad request URLs" in {
      val id = 1L
      val callFuture = typedQuery(GetAccountBalances(id), badBaseURL)
      val responseFuture = typedResponse(callFuture, mapPlain, mapMoneyMarket)
      whenReady(responseFuture, timeout) { result =>
        result should equal(Left(
          "FAIL 404 Not Found The requested resource could not be found."))
      }
    }
  }
}