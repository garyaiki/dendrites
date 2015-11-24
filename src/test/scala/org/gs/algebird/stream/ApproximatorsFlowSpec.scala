/**
  */
package org.gs.algebird.stream

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import com.twitter.algebird.CMSHasherImplicits._
import com.twitter.algebird._
import org.scalatest.{ WordSpecLike, Matchers }
import org.scalatest._
import org.scalatest.Matchers._

import org.gs.aggregator._
import org.gs.algebird._
import org.gs.algebird.agent.{ AveragedAgent,
  CountMinSketchAgent,
  DecayedValueAgent,
  HyperLogLogAgent,
  QTreeAgent }
import org.gs.fixtures.TestValuesBuilder


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

  "A BigDecimals ApproximatorsFlow" should {
    "update all agents and return their latest values" in {
      implicit val qtBDSemigroup = new QTreeSemigroup[BigDecimal](qTreeLevel)
      val bdFlow = new ApproximatorsFlow[BigDecimal](
        new AveragedAgent("test approximators Averaged Value Agent"),
        new CountMinSketchAgent[BigDecimal]("test approximators Count Min Sketch Agent"),
        new DecayedValueAgent("test approximators DecayedValue Agent", 10.0),
        new HyperLogLogAgent("test approximators HyperLogLog Agent"),
        new QTreeAgent[BigDecimal]("test approximators QTree Agent"))
        
      val (pub, sub) = TestSource.probe[Seq[BigDecimal]]
        .via(bdFlow.approximators)
        .toMat(TestSink.probe[(com.twitter.algebird.AveragedValue,
            com.twitter.algebird.CMS[BigDecimal],
            Seq[com.twitter.algebird.DecayedValue],
            com.twitter.algebird.HLL,
            com.twitter.algebird.QTree[BigDecimal])])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(bigDecimals)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      response._1 shouldBe an [AveragedValue]
      response._2 shouldBe an [CMS[_]]
      response._3(0) shouldBe an [DecayedValue]
      response._4 shouldBe an [HLL]
      response._5 shouldBe an [QTree[_]]
    }
  }

  "A BigInts ApproximatorsFlow" should {
    "update all agents and return their latest values" in {
      implicit val qtBISemigroup = new QTreeSemigroup[BigInt](qTreeLevel)
      val biFlow = new ApproximatorsFlow[BigInt](
        new AveragedAgent("test approximators Averaged Value Agent"),
        new CountMinSketchAgent[BigInt]("test approximators Count Min Sketch Agent"),
        new DecayedValueAgent("test approximators DecayedValue Agent", 10.0),
        new HyperLogLogAgent("test approximators HyperLogLog Agent"),
        new QTreeAgent[BigInt]("test approximators QTree Agent"))
        
      val (pub, sub) = TestSource.probe[Seq[BigInt]]
        .via(biFlow.approximators)
        .toMat(TestSink.probe[(com.twitter.algebird.AveragedValue,
            com.twitter.algebird.CMS[BigInt],
            Seq[com.twitter.algebird.DecayedValue],
            com.twitter.algebird.HLL,
            com.twitter.algebird.QTree[BigInt])])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(bigInts)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      response._1 shouldBe an [AveragedValue]
      response._2 shouldBe an [CMS[_]]
      response._3(0) shouldBe an [DecayedValue]
      response._4 shouldBe an [HLL]
      response._5 shouldBe an [QTree[_]]
    }
  }

  "A Doubles ApproximatorsFlow" should {
    "update all agents and return their latest values" in {
      implicit val qtBISemigroup = new QTreeSemigroup[Double](qTreeLevel)
      val biFlow = new ApproximatorsFlow[Double](
        new AveragedAgent("test approximators Averaged Value Agent"),
        new CountMinSketchAgent[Double]("test approximators Count Min Sketch Agent"),
        new DecayedValueAgent("test approximators DecayedValue Agent", 10.0),
        new HyperLogLogAgent("test approximators HyperLogLog Agent"),
        new QTreeAgent[Double]("test approximators QTree Agent"))
        
      val (pub, sub) = TestSource.probe[Seq[Double]]
        .via(biFlow.approximators)
        .toMat(TestSink.probe[(com.twitter.algebird.AveragedValue,
            com.twitter.algebird.CMS[Double],
            Seq[com.twitter.algebird.DecayedValue],
            com.twitter.algebird.HLL,
            com.twitter.algebird.QTree[Double])])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(doubles)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      response._1 shouldBe an [AveragedValue]
      response._2 shouldBe an [CMS[_]]
      response._3(0) shouldBe an [DecayedValue]
      response._4 shouldBe an [HLL]
      response._5 shouldBe an [QTree[_]]
    }
  }

  "A Floats ApproximatorsFlow" should {
    "update all agents and return their latest values" in {
      implicit val qtBISemigroup = new QTreeSemigroup[Float](qTreeLevel)
      val biFlow = new ApproximatorsFlow[Float](
        new AveragedAgent("test approximators Averaged Value Agent"),
        new CountMinSketchAgent[Float]("test approximators Count Min Sketch Agent"),
        new DecayedValueAgent("test approximators DecayedValue Agent", 10.0),
        new HyperLogLogAgent("test approximators HyperLogLog Agent"),
        new QTreeAgent[Float]("test approximators QTree Agent"))
        
      val (pub, sub) = TestSource.probe[Seq[Float]]
        .via(biFlow.approximators)
        .toMat(TestSink.probe[(com.twitter.algebird.AveragedValue,
            com.twitter.algebird.CMS[Float],
            Seq[com.twitter.algebird.DecayedValue],
            com.twitter.algebird.HLL,
            com.twitter.algebird.QTree[Float])])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(floats)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      response._1 shouldBe an [AveragedValue]
      response._2 shouldBe an [CMS[_]]
      response._3(0) shouldBe an [DecayedValue]
      response._4 shouldBe an [HLL]
      response._5 shouldBe an [QTree[_]]
    }
  }

  "A Ints ApproximatorsFlow" should {
    "update all agents and return their latest values" in {
      implicit val qtBISemigroup = new QTreeSemigroup[Int](qTreeLevel)
      val biFlow = new ApproximatorsFlow[Int](
        new AveragedAgent("test approximators Averaged Value Agent"),
        new CountMinSketchAgent[Int]("test approximators Count Min Sketch Agent"),
        new DecayedValueAgent("test approximators DecayedValue Agent", 10.0),
        new HyperLogLogAgent("test approximators HyperLogLog Agent"),
        new QTreeAgent[Int]("test approximators QTree Agent"))
        
      val (pub, sub) = TestSource.probe[Seq[Int]]
        .via(biFlow.approximators)
        .toMat(TestSink.probe[(com.twitter.algebird.AveragedValue,
            com.twitter.algebird.CMS[Int],
            Seq[com.twitter.algebird.DecayedValue],
            com.twitter.algebird.HLL,
            com.twitter.algebird.QTree[Int])])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(ints)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      response._1 shouldBe an [AveragedValue]
      response._2 shouldBe an [CMS[_]]
      response._3(0) shouldBe an [DecayedValue]
      response._4 shouldBe an [HLL]
      response._5 shouldBe an [QTree[_]]
    }
  }

  "A Longs ApproximatorsFlow" should {
    "update all agents and return their latest values" in {
      implicit val qtBISemigroup = new QTreeSemigroup[Long](qTreeLevel)
      val biFlow = new ApproximatorsFlow[Long](
        new AveragedAgent("test approximators Averaged Value Agent"),
        new CountMinSketchAgent[Long]("test approximators Count Min Sketch Agent"),
        new DecayedValueAgent("test approximators DecayedValue Agent", 10.0),
        new HyperLogLogAgent("test approximators HyperLogLog Agent"),
        new QTreeAgent[Long]("test approximators QTree Agent"))
        
      val (pub, sub) = TestSource.probe[Seq[Long]]
        .via(biFlow.approximators)
        .toMat(TestSink.probe[(com.twitter.algebird.AveragedValue,
            com.twitter.algebird.CMS[Long],
            Seq[com.twitter.algebird.DecayedValue],
            com.twitter.algebird.HLL,
            com.twitter.algebird.QTree[Long])])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(longs)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      response._1 shouldBe an [AveragedValue]
      response._2 shouldBe an [CMS[_]]
      response._3(0) shouldBe an [DecayedValue]
      response._4 shouldBe an [HLL]
      response._5 shouldBe an [QTree[_]]
    }
  }
}
