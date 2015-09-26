package org.gs.examples.account.http
import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.http.scaladsl.unmarshalling.Unmarshaller.UnsupportedContentTypeException
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

class BalancesClientsSpec extends WordSpecLike with Matchers with BalancesClients {
  implicit val system = ActorSystem("akka-aggregator")
  override implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val executor = Executors.newSingleThreadExecutor()
  val client = new CheckingBalancesClient()
  val hostConfig = client.hostConfig
  val config = hostConfig._1
  val flow = ClientConnectionPool(hostConfig._2, hostConfig._3)
  val baseURL = client.baseURL
  val badBaseURL = baseURL.dropRight(1)

  val timeout = Timeout(3000 millis)

  "A Generic CheckingBalancesClient" should {
    "get balances for id 1" in {
      val id = 1L
      val callFuture = HigherOrderCalls.call(GetAccountBalances(id), baseURL)
      val responseFuture = HigherOrderCalls.byId(id, callFuture, client.mapChecking, mapPlain)

      whenReady(responseFuture, timeout) { result =>
        result should equal(Right(CheckingAccountBalances(Some(List((1, 1000.1))))))
      }
    }
  }

  it should {
    "get balances for id 2" in {
      val id = 2L
      val callFuture = HigherOrderCalls.call(GetAccountBalances(id), baseURL)
      val responseFuture = HigherOrderCalls.byId(id, callFuture, client.mapChecking, mapPlain)
      whenReady(responseFuture, timeout) { result =>
        result should equal(Right(CheckingAccountBalances(Some(List((2L, BigDecimal(2000.20)),
          (22L, BigDecimal(2200.22)))))))
      }
    }
  }

  it should {
    "get balances for id 3" in {
      val id = 3L
      val callFuture = HigherOrderCalls.call(GetAccountBalances(id), baseURL)
      val responseFuture = HigherOrderCalls.byId(id, callFuture, client.mapChecking, mapPlain)
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
      val callFuture = HigherOrderCalls.call(GetAccountBalances(id), baseURL)
      val responseFuture = HigherOrderCalls.byId(id, callFuture, client.mapChecking, mapPlain)
      whenReady(responseFuture, timeout) { result =>
        result should equal(Left("Checking account 4 not found"))
      }
    }
  }

  it should {
    "fail bad request URLs" in {
      val id = 1L
      val callFuture = HigherOrderCalls.call(GetAccountBalances(id), badBaseURL)
      //val client = new CheckingBalancesClient()
      val responseFuture = HigherOrderCalls.byId(id, callFuture, client.mapChecking, mapPlain)
      whenReady(responseFuture, timeout) { result =>
        result should equal(Left(
          "FAIL id:1 404 Not Found The requested resource could not be found."))
      }
    }
  }
  
  "A CheckingBalancesClient" should {
    "get balances for id 1" in {

      val futureResultChecking = client.requestCheckingBalances(1L, baseURL, client.mapChecking)

      whenReady(futureResultChecking, timeout) { result =>
        result should equal(Right(CheckingAccountBalances(Some(List((1, 1000.1))))))
      }
    }
  }

  it should {
    "get balances for id 2" in {

      val futureResultChecking = client.requestCheckingBalances(2L, baseURL, client.mapChecking)
      whenReady(futureResultChecking, timeout) { result =>
        result should equal(Right(CheckingAccountBalances(Some(List((2L, BigDecimal(2000.20)),
          (22L, BigDecimal(2200.22)))))))
      }
    }
  }

  it should {
    "get balances for id 3" in {
      val futureResultChecking = client.requestCheckingBalances(3L, baseURL, client.mapChecking)
      whenReady(futureResultChecking, timeout) { result =>
        result should equal(Right(CheckingAccountBalances(Some(List((3L, BigDecimal(3000.30)),
          (33L, BigDecimal(3300.33)),
          (333L, BigDecimal(3330.33)))))))
      }
    }
  }

  it should {
    "not find bad ids" in {
      val futureResultChecking = client.requestCheckingBalances(4L, baseURL, client.mapChecking)
      whenReady(futureResultChecking, timeout) { result =>
        result should equal(Left("Checking account 4 not found"))
      }
    }
  }

  it should {
    "fail bad request URLs" in {
      val futureResultChecking = client.requestCheckingBalances(1L, badBaseURL, client.mapChecking)
      whenReady(futureResultChecking, timeout) { result =>
        result should equal(Left(
          "FAIL id:1 404 Not Found The requested resource could not be found."))
      }
    }
  }
  /*
  "A MoneyMarketBalancesClient" should {
    "get balances for id 1" in {

      val futureResultMM = client.requestMMBalances(1L, baseURL)
//      val futureResultSavings = client.requestSavingsBalances(1L, baseURL)

      whenReady(futureResultMM, timeout) { result =>
        result should equal(Right(MoneyMarketAccountBalances(Some(List((1, 11000.1))))))
      }
      whenReady(futureResultSavings, timeout) { result =>
        result should equal(Right(SavingsAccountBalances(Some(List((1, 111000.1))))))
      }
    }
  }

  it should {
    "get balances for id 2" in {

      val futureResultChecking = client.requestCheckingBalances(2L, baseURL)
      whenReady(futureResultChecking, timeout) { result =>
        result should equal(Right(CheckingAccountBalances(Some(List((2L, BigDecimal(2000.20)),
          (22L, BigDecimal(2200.22)))))))
      }
    }
  }

  it should {
    "get balances for id 3" in {
      val futureResultChecking = client.requestCheckingBalances(3L, baseURL)
      whenReady(futureResultChecking, timeout) { result =>
        result should equal(Right(CheckingAccountBalances(Some(List((3L, BigDecimal(3000.30)),
          (33L, BigDecimal(3300.33)),
          (333L, BigDecimal(3330.33)))))))
      }
    }
  }

  it should {
    "not find bad ids" in {
      val futureResultChecking = client.requestCheckingBalances(4L, baseURL)
      whenReady(futureResultChecking, timeout) { result =>
        result should equal(Left("Checking account 4 not found"))
      }
    }
  }

  it should {
    "fail bad request URLs" in {
      val futureResultChecking = client.requestCheckingBalances(1L, badBaseURL)
      whenReady(futureResultChecking, timeout) { result =>
        result should equal(Left(
          "FAIL id:1 404 Not Found The requested resource could not be found."))
      }
    }
  }
*/
}