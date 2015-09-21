package org.gs.examples.account.http
import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.http.scaladsl.unmarshalling.Unmarshaller.UnsupportedContentTypeException
import akka.stream.ActorMaterializer
import java.util.concurrent.Executors
import org.gs.akka.http.ClientConnectionPool
import org.gs.examples.account.{ AccountType, Checking, CheckingAccountBalances, GetAccountBalances }
import org.gs.testdriven.StopSystemAfterAll
import org.scalatest.{ Matchers, WordSpecLike }
import org.scalatest._
import matchers.ShouldMatchers._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.collection.mutable.ArrayBuffer
import scala.util.{ Failure, Success, Try }
import org.gs.examples.account.httpakka.CheckingAccountClient._

class BalancesClientsSpec extends WordSpecLike
  with Matchers
  with BalancesClients {

  val hostConfig = CheckingBalancesClient.getHostConfig()
  val config = hostConfig._1
  val flow = ClientConnectionPool(hostConfig._2, hostConfig._3)
  val baseURL = CheckingBalancesClient.configBaseUrl(hostConfig)

  implicit val system = ActorSystem("akka-aggregator")
  override implicit val materializer = ActorMaterializer()
  override val logger = Logging(system, getClass)
  val client = CheckingBalancesClient()
  val timeout = Timeout(3000 millis)
  implicit val executor = Executors.newSingleThreadExecutor()

  "A BalancesClients" should {
    "get balances for id 1" in {

      val futureResult = client.requestCheckingBalances(1L, baseURL)
      whenReady(futureResult, timeout) { result =>
        result should equal(Right(CheckingAccountBalances(Some(List((1, 1000.1))))))
      }
    }
  }

  it should {
    "get balances for id 2" in {

      val futureResult = client.requestCheckingBalances(2L, baseURL)
      whenReady(futureResult, timeout) { result =>
        result should equal(Right(CheckingAccountBalances(Some(List((2L, BigDecimal(2000.20)),
          (22L, BigDecimal(2200.22)))))))
      }
    }
  }

  it should {
    "get balances for id 3" in {
      val futureResult = client.requestCheckingBalances(3L, baseURL)
      whenReady(futureResult, timeout) { result =>
        result should equal(Right(CheckingAccountBalances(Some(List((3L, BigDecimal(3000.30)),
          (33L, BigDecimal(3300.33)),
          (333L, BigDecimal(3330.33)))))))
      }
    }
  }
 
  it should {
    "throw UnsupportedContentTypeException for bad ids" in {
      val futureResult = client.requestCheckingBalances(4L, baseURL)
      whenReady(futureResult.failed, timeout) { e =>
        e shouldBe a [UnsupportedContentTypeException]
      }
    }
  }

}