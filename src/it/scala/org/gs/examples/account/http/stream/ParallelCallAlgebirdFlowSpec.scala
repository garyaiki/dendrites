package org.gs.examples.account.http.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Keep }
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import com.twitter.algebird._
import org.scalatest.{ Matchers, WordSpecLike }
import org.scalatest._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.reflect.runtime.universe
import org.gs.algebird.stream.CreateHLLFlow
import org.gs.algebird.stream.{avgFlow, maxFlow, minFlow}
import org.gs.examples.account.GetAccountBalances
import org.gs.examples.account._
import org.gs.examples.account.stream._

class ParallelCallAlgebirdFlowSpec extends WordSpecLike with Matchers {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val timeout = Timeout(3000 millis)

  def source = TestSource.probe[Product]
  def sinkLeftRight = TestSink.probe[(Seq[String], Seq[AnyRef])]
  val pcf = new ParallelCallFlow
  val wrappedFlow = pcf.wrappedCallsLRFlow
  var rightResponse: Option[Seq[AnyRef]] = None
/*
  def exf: Flow[Seq[AnyRef], Seq[BigDecimal], NotUsed] =
    Flow[Seq[AnyRef]].map(extractBalancesVals[BigDecimal])

  def source3 = TestSource.probe[Seq[BigDecimal]]

  val sink3 = TestSink.probe[AveragedValue]
*/
  "A ParallelCallAlgebirdFlowClient" should {
    "get balances for id 1" in {
      val id = 1L
      val (pub, sub) = source
        .via(wrappedFlow)
        .toMat(sinkLeftRight)(Keep.both).run()
      sub.request(1)
      pub.sendNext(GetAccountBalances(id))
      val response = sub.expectNext
      pub.sendComplete()
      sub.expectComplete()
      val balancesLists = extractBalancesVals(response._2)
      balancesLists should equal(List(1000.1, 11000.1, 111000.1))
      //println(s"response2:$response2")
      rightResponse = Some(response._2)
    }

  def source2 = TestSource.probe[Seq[AnyRef]]
  def sink2 = TestSink.probe[Seq[BigDecimal]]
  var balancesValues: Option[Seq[BigDecimal]] = None
    "extract balances from the Right response" in {
      val (pub2, sub2) = source2
        .via(extractBalancesFlow)
        .toMat(sink2)(Keep.both).run()
      sub2.request(1)
      pub2.sendNext(rightResponse.get)
      val response2 = sub2.expectNext
      pub2.sendComplete()
      sub2.expectComplete()
      response2 should equal(List(1000.1, 11000.1, 111000.1))
      balancesValues = Some(response2)
    }

    "get averaged value from the Right response" in {
      val (pub2, sub2) = source2
        .via(extractBalancesFlow)
        .via(avgFlow[BigDecimal])
        .toMat(TestSink.probe[AveragedValue])(Keep.both).run()
      sub2.request(1)
      pub2.sendNext(rightResponse.get)
      val response3 = sub2.expectNext
      println(s"avg:$response3")
      pub2.sendComplete()
      sub2.expectComplete()
      response3 should equal(AveragedValue(3, 41000.1))
    }

    "get a HyperLogLog from the Right response" in {
      val (pub2, sub2) = source2
        .via(extractBalancesFlow)
        .via(new CreateHLLFlow[BigDecimal])
        .toMat(TestSink.probe[HLL])(Keep.both).run()
      sub2.request(1)
      pub2.sendNext(rightResponse.get)
      val response3 = sub2.expectNext
      println(s"avg:$response3")
      pub2.sendComplete()
      sub2.expectComplete()
      response3.estimatedSize should equal(balancesValues.get.distinct.size.toDouble +- 0.09)
    }

    "get the Max from the Right response" in {
      val (pub2, sub2) = source2
        .via(extractBalancesFlow)
        .via(maxFlow[BigDecimal])
        .toMat(TestSink.probe[BigDecimal])(Keep.both).run()
      sub2.request(1)
      pub2.sendNext(rightResponse.get)
      val response3 = sub2.expectNext
      println(s"avg:$response3")
      pub2.sendComplete()
      sub2.expectComplete()
      response3 should equal(111000.1)
    }

    "get the Min from the Right response" in {
      val (pub2, sub2) = source2
        .via(extractBalancesFlow)
        .via(minFlow[BigDecimal])
        .toMat(TestSink.probe[BigDecimal])(Keep.both).run()
      sub2.request(1)
      pub2.sendNext(rightResponse.get)
      val response3 = sub2.expectNext
      println(s"avg:$response3")
      pub2.sendComplete()
      sub2.expectComplete()
      response3 should equal(1000.1)
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
      val response = sub.expectNext
      pub.sendComplete()
      sub.expectComplete()
      val balancesLists = extractBalancesVals(response._2)
      balancesLists should equal(List(2000.2, 2200.22, 22000.2, 22200.22, 222000.2, 222200.22))
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
      val response = sub.expectNext
      pub.sendComplete()
      sub.expectComplete()
      val balancesLists = extractBalancesVals(response._2)
      balancesLists should equal(
        List(3000.3, 3300.33, 3330.33, 33000.3, 33300.33, 33330.33, 333000.3, 333300.33, 333330.33))
    }
  }
}
