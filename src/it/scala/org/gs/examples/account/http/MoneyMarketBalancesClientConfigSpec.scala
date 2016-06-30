package org.gs.examples.account.http
import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import java.util.concurrent.Executors
import org.scalatest.{ Matchers, WordSpecLike }
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import org.gs.examples.account.http.actor.CheckingAccountClient._
import scala.math.BigDecimal.double2bigDecimal
import org.gs.examples.account.{GetAccountBalances, MoneyMarketAccountBalances}
import org.gs.http.{caseClassToGetQuery, typedQuery, typedResponse}

/**
  *
  * @author Gary Struthers
  */ 
class MoneyMarketBalancesClientConfigSpec extends WordSpecLike with Matchers with BalancesProtocols {
  implicit val system = ActorSystem("dendrites")
  override implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val executor = Executors.newSingleThreadExecutor()
  val clientConfig = new MoneyMarketBalancesClientConfig()
  val hostConfig = clientConfig.hostConfig
  val config = hostConfig._1
  val baseURL = clientConfig.baseURL
  val badBaseURL = baseURL.dropRight(1)

  val timeout = Timeout(3000 millis)

  "A MoneyBalancesClient" should {
    "get balances for id 1" in {
      val id = 1L
      val cc = GetAccountBalances(id)
      val callFuture = typedQuery(baseURL, cc.productPrefix, caseClassToGetQuery)(cc)
      val responseFuture = typedResponse(mapPlain, mapMoneyMarket)(callFuture)

      whenReady(responseFuture, timeout) { result =>
        result should equal(Right(MoneyMarketAccountBalances[BigDecimal](Some(List((1, 11000.1))))))
      }
    }
  }

  it should {
    "get balances for id 2" in {
      val id = 2L
      val cc = GetAccountBalances(id)
      val callFuture = typedQuery(baseURL, cc.productPrefix, caseClassToGetQuery)(cc)
      val responseFuture = typedResponse(mapPlain, mapMoneyMarket)(callFuture)

      whenReady(responseFuture, timeout) { result =>
        result should equal(Right(MoneyMarketAccountBalances(Some(List((2L, BigDecimal(22000.20)),
          (22L, BigDecimal(22200.22)))))))
      }
    }
  }

  it should {
    "get balances for id 3" in {
      val id = 3L
      val cc = GetAccountBalances(id)
      val callFuture = typedQuery(baseURL, cc.productPrefix, caseClassToGetQuery)(cc)
      val responseFuture = typedResponse(mapPlain, mapMoneyMarket)(callFuture)

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
      val cc = GetAccountBalances(id)
      val callFuture = typedQuery(baseURL, cc.productPrefix, caseClassToGetQuery)(cc)
      val responseFuture = typedResponse(mapPlain, mapMoneyMarket)(callFuture)

      whenReady(responseFuture, timeout) { result =>
        result should equal(Left("Money Market account 4 not found"))
      }
    }
  }

  it should {
    "fail bad request URLs" in {
      val id = 1L
      val cc = GetAccountBalances(id)
      val callFuture = typedQuery(badBaseURL, cc.productPrefix, caseClassToGetQuery)(cc)
      val responseFuture = typedResponse(mapPlain, mapMoneyMarket)(callFuture)

      whenReady(responseFuture, timeout) { result =>
        result should equal(Left(
          "FAIL 404 Not Found The requested resource could not be found."))
      }
    }
  }
}
