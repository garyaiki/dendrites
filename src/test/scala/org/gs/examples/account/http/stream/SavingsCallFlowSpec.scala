package org.gs.examples.account.http.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Flow}
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
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
import org.gs.examples.account.http._
import org.gs.http._
import org.gs.testdriven.StopSystemAfterAll
import org.scalatest.{ Matchers, WordSpecLike }
import org.scalatest._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

class SavingsCallFlowSpec extends WordSpecLike with Matchers with BalancesProtocols {
  implicit val system = ActorSystem("dendrites")
  override implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val executor = Executors.newSingleThreadExecutor()
  val timeout = Timeout(3000 millis)
  
  def source = TestSource.probe[Product]
  def sink = TestSink.probe[Either[String, AnyRef]]
  val scf = new SavingsCallFlow
  val testFlow = source.via(scf.flow).toMat(sink)(Keep.both)
  
  "A SavingsCallFlowClient" should {
    "get balances for id 1" in {
      val id = 1L
      val (pub, sub) = testFlow.run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      response should equal(Right(SavingsAccountBalances[BigDecimal](Some(List((1, 111000.1))))))
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
      
      response should equal(Right(SavingsAccountBalances(Some(List((2L, BigDecimal(222000.20)),
              (22L, BigDecimal(222200.22)))))))
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
      
      response should equal(Right(SavingsAccountBalances(Some(List((3L, BigDecimal(333000.30)),
          (33L, BigDecimal(333300.33)),
          (333L, BigDecimal(333330.33)))))))
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
      
      response should equal(Left("Savings account 4 not found"))
    }
  }

  val clientConfig = new SavingsBalancesClientConfig()
  val badBaseURL = clientConfig.baseURL.dropRight(1)
  def badPartial = typedQueryResponse(badBaseURL, mapPlain, mapSavings) _
  def badFlow: Flow[Product, Either[String, AnyRef], Unit] = Flow[Product].mapAsync(1)(badPartial)

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
