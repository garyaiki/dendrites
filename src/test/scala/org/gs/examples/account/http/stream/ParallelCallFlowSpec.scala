package org.gs.examples.account.http.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.{ActorAttributes, ActorMaterializer, FlowShape, UniformFanOutShape}
import akka.stream.scaladsl.{ Broadcast, Keep, Flow, FlowGraph, Source, Zip }
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
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

class ParallelCallFlowSpec extends WordSpecLike with Matchers with BalancesProtocols {
  implicit val system = ActorSystem("akka-aggregator")
  override implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val timeout = Timeout(3000 millis)

  val checkingClientConfig = new CheckingBalancesClientConfig()
  val checkingHostConfig = checkingClientConfig.hostConfig
  val checkingBaseURL = checkingClientConfig.baseURL
  val checkingBadBaseURL = checkingBaseURL.dropRight(1)

  def checkingPartial = typedQueryResponse(checkingBaseURL, mapPlain, mapChecking) _
  def checkingBadPartial = typedQueryResponse(checkingBadBaseURL, mapPlain, mapChecking) _

  def checkingFlow: Flow[Product, Either[String, AnyRef], Unit] = Flow[Product].mapAsync(1)(checkingPartial)
  def checkingBadFlow: Flow[Product, Either[String, AnyRef], Unit] = Flow[Product].mapAsync(1)(checkingBadPartial)
  
  val moneyMarketClientConfig = new MoneyMarketBalancesClientConfig()
  val moneyMarketHostConfig = moneyMarketClientConfig.hostConfig
  val moneyMarketBaseURL = moneyMarketClientConfig.baseURL
  val moneyMarketBadBaseURL = moneyMarketBaseURL.dropRight(1)

  def moneyMarketPartial = typedQueryResponse(moneyMarketBaseURL, mapPlain, mapMoneyMarket) _
  def moneyMarketBadPartial = typedQueryResponse(moneyMarketBadBaseURL, mapPlain, mapMoneyMarket) _
  def moneyMarketFlow: Flow[Product, Either[String, AnyRef], Unit] = Flow[Product].mapAsync(1)(moneyMarketPartial)
  def moneyMarketBadFlow: Flow[Product, Future[Either[String, AnyRef]], Unit] = Flow[Product].map(moneyMarketBadPartial)
  def moneyMarketAsyncFlow: Flow[Product, Either[String, AnyRef], Unit] = Flow[Product].mapAsync(2)(moneyMarketPartial)

  val savingsClientConfig = new SavingsBalancesClientConfig()
  val savingsHostConfig = savingsClientConfig.hostConfig
  val savingsBaseURL = savingsClientConfig.baseURL
  val savingsBadBaseURL = savingsBaseURL.dropRight(1)
  
  def source = TestSource.probe[Product]
  def sink = TestSink.probe[(Either[String, AnyRef],Either[String, AnyRef])]
  def zipper = Zip[Either[String, AnyRef],Either[String, AnyRef]]
  import FlowGraph.Implicits._ 

  val fg = FlowGraph.partial() { implicit builder =>
    val bcast: UniformFanOutShape[Product, Product] = builder.add(Broadcast[Product](2))
    val check: FlowShape[Product,Either[String, AnyRef]] = builder.add(checkingFlow)
    val mm: FlowShape[Product,Either[String, AnyRef]] = builder.add(moneyMarketFlow)
    val zip = builder.add(zipper)
    
    bcast ~> check ~> zip.in0
    bcast ~> mm ~> zip.in1
    FlowShape(bcast.in, zip.out)
  }.named("partial")
  val wrappedFlow = Flow.wrap(fg)
  
  "A ParallelCallFlowClient" should {
    "get balances for id 1" in {
      val id = 1L
      val (pub, sub) = source
      .via(wrappedFlow)
      .toMat(sink)(Keep.both).run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      response should equal(Right(CheckingAccountBalances(Some(List((1,1000.1))))),Right(MoneyMarketAccountBalances(Some(List((1,11000.1))))))
    }
  }
/*
  it should {
    "get balances for id 2" in {
      val id = 2L
      val (pub, sub) = testFlow.run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val responseFuture = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      whenReady(responseFuture, timeout) { result =>
        result should equal(Right(CheckingAccountBalances(Some(List((2L, BigDecimal(2000.20)),
          (22L, BigDecimal(2200.22)))))))
      }
    }
  }

  it should {
    "get balances for id 3" in {
      val id = 3L
      val (pub, sub) = testFlow.run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val responseFuture = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

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
      val (pub, sub) = testFlow.run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val responseFuture = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      whenReady(responseFuture, timeout) { result =>
        result should equal(Left("Checking account 4 not found"))
      }
    }
  }

  it should {
    "fail bad request URLs" in {
      val id = 1L
      val (pub, sub) = source
        .via(checkingBadFlow)
        .toMat(sink)(Keep.both).run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val responseFuture = sub.expectNext()
      whenReady(responseFuture, timeout) { result =>
        result should equal(Left(
          "FAIL 404 Not Found The requested resource could not be found."))
      }
    }
  }*/
}