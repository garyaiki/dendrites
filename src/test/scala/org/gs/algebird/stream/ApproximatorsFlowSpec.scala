/**
  */
package org.gs.algebird.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import com.twitter.algebird._
import org.scalatest.{ WordSpecLike, Matchers }
import org.gs.algebird._
import org.gs.algebird.agent.{AveragedAgent, CountMinSketchAgent, DecayedValueAgent, HyperLogLogAgent, QTreeAgent}
import org.gs.fixtures.TestValuesBuilder
import org.gs.stream._
import scala.concurrent.ExecutionContext.Implicits.global

/** @author garystruthers
  *
  */
class ApproximatorsSpec extends WordSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val m = DecayedValueMonoid(0.001)
  implicit val ag = HyperLogLogAggregator(12)
  implicit val monoid = new HyperLogLogMonoid(12)
  val qTreeLevel = 16
  implicit val qtBDSemigroup = new QTreeSemigroup[BigDecimal](qTreeLevel)
  val af = new ApproximatorsFlow[BigDecimal](
        new AveragedAgent("test approximators Averaged Value Agent"),
        new CountMinSketchAgent[BigDecimal]("test approximators Count Min Sketch Agent"),
        new DecayedValueAgent("test approximators DecayedValue Agent", 10.0),
        new HyperLogLogAgent("test approximators HyperLogLog Agent"),
        new QTreeAgent[BigDecimal]("test approximators QTree Agent"))
  "ApproximatorsFlow" should {
    "update all agents and return their latest values" in {

      val (pub, sub) = TestSource.probe[Seq[BigDecimal]]
        .via(af.approximators)
        .toMat(TestSink.probe[(com.twitter.algebird.AveragedValue, com.twitter.algebird.CMS[BigDecimal], Seq[com.twitter.algebird.DecayedValue], com.twitter.algebird.HLL, com.twitter.algebird.QTree[BigDecimal])])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(bigDecimals)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      println(s"response:${response._1}")
      //println(s"response:${response._2}")
      println(s"response:${response._3}")
      println(s"response:${response._4}")
      //println(s"response:${response._5}")
    }
  }
/*
  "A Sequence of String" should {
    "return its Max" in {

      val (pub, sub) = TestSource.probe[Seq[String]]
        .via(maxFlow)
        .toMat(TestSink.probe[String])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(strings)
      sub.expectNext(strings.max)
      pub.sendComplete()
      sub.expectComplete()
    }
  }

  "A Sequence of Option[BigInt]" should {
    "return its Max" in {

      val (pub, sub) = TestSource.probe[Seq[Option[BigInt]]]
        .via(flattenFlow)
        .via(maxFlow)
        .toMat(TestSink.probe[BigInt])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(optBigInts)
      sub.expectNext(optBigInts.flatten.max)
      pub.sendComplete()
      sub.expectComplete()
    }
  }

  "A Sequence of Either[String, Double]" should {
    "return its Max" in {

      val (pub, sub) = TestSource.probe[Seq[Either[String, Double]]]
        .via(collectRightFlow)
        .via(maxFlow)
        .toMat(TestSink.probe[Double])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(eithDoubles)
      sub.expectNext(doubles.max)
      pub.sendComplete()
      sub.expectComplete()
    }
  }*/
}
