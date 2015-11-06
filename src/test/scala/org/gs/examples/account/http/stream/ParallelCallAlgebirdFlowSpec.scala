package org.gs.examples.account.http.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import java.util.concurrent.Executors
import org.gs.http.ClientConnectionPool
import org.gs.examples.account.{CheckingAccountBalances,
                                GetAccountBalances,
                                MoneyMarketAccountBalances,
                                SavingsAccountBalances}
import org.gs.examples.account._
import org.gs.testdriven.StopSystemAfterAll
import org.scalatest.{ Matchers, WordSpecLike }
import org.scalatest._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._

class ParallelCallAlgebirdFlowSpec extends WordSpecLike with Matchers {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val timeout = Timeout(3000 millis)
  
  def source = TestSource.probe[Product]
  def sinkLeftRight = TestSink.probe[(Seq[String],Seq[AnyRef])]
  val pcf = new ParallelCallFlow
  val wrappedFlow = pcf.wrappedCallsLRFlow

  
  "A ParallelCallAlgebirdFlowClient" should {
    "get balances for id 1" in {
      val id = 1L
      val (pub, sub) = source
      .via(wrappedFlow)
      .toMat(sinkLeftRight)(Keep.both).run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      val balancesLists = extractBalancesLists(response._2)
      balancesLists.flatten should equal (List(1000.1, 11000.1, 111000.1))
    }
  }

  it should {
    "get balances for id 2" in {
      val id = 2L
      val (pub, sub) = source
      .via(wrappedFlow)
      .toMat(sinkLeftRight)(Keep.both).run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      val balancesLists = extractBalancesLists(response._2)
      balancesLists.flatten should equal(List(2000.2,2200.22,22000.2,22200.22,222000.2,222200.22))
    }
  }

  it should {
    "get balances for id 3" in {
      val id = 3L
      val (pub, sub) = source
      .via(wrappedFlow)
      .toMat(sinkLeftRight)(Keep.both).run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      val balancesLists = extractBalancesLists(response._2)
      balancesLists.flatten should equal(
          List(3000.3,3300.33,3330.33,33000.3,33300.33,33330.33,333000.3,333300.33,333330.33))
    }
  }
}
