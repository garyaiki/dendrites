/**
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.github.garyaiki.dendrites.algebird.agent.stream

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.twitter.algebird.CMSHasherImplicits._
import com.twitter.algebird.{AveragedValue, CMS, DecayedValue, HLL, QTree, QTreeSemigroup}
import org.scalatest.{Matchers,WordSpecLike}
import org.scalatest.Matchers._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import com.github.garyaiki.dendrites.algebird.AlgebirdConfigurer
import com.github.garyaiki.dendrites.algebird.{cmsHasherBigDecimal, cmsHasherDouble, cmsHasherFloat, createHLL}
import com.github.garyaiki.dendrites.algebird.agent.Agents
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder

/**
  *
  * @author Gary Struthers
  */
class ApproximatorsFlowSpec extends WordSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val halfLife = AlgebirdConfigurer.decayedValueHalfLife
  implicit val m = AlgebirdConfigurer.decayedValueMonoid
  implicit val ag = AlgebirdConfigurer.hyperLogLogAgggregator
  implicit val monoid = AlgebirdConfigurer.hyperLogLogMonoid
  val qTreeLevel = AlgebirdConfigurer.qTreeLevel

  "A BigDecimals ApproximatorsFlow" should {
    "update all agents and return their latest values" in {
      implicit val qtBDSemigroup = new QTreeSemigroup[BigDecimal](qTreeLevel)
      val agents = new Agents[BigDecimal]("test BigDecimal approximators agents")
      val avgAgent = agents.avgAgent
      val cmsAgent = agents.cmsAgent
      val dvAgent = agents.dcaAgent
      val hllAgent = agents.hllAgent
      val qtAgent = agents.qtAgent
      val bdFlow = new ApproximatorsFlow[BigDecimal](avgAgent, cmsAgent, dvAgent, hllAgent, qtAgent)

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
      val agents = new Agents[BigInt]("test BigInt approximators agents")
      val avgAgent = agents.avgAgent
      val cmsAgent = agents.cmsAgent
      val dvAgent = agents.dcaAgent
      val hllAgent = agents.hllAgent
      val qtAgent = agents.qtAgent
      val biFlow = new ApproximatorsFlow[BigInt](avgAgent, cmsAgent, dvAgent, hllAgent, qtAgent)

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
      val agents = new Agents[Double]("test Double approximators agents")
      val avgAgent = agents.avgAgent
      val cmsAgent = agents.cmsAgent
      val dvAgent = agents.dcaAgent
      val hllAgent = agents.hllAgent
      val qtAgent = agents.qtAgent
      val biFlow = new ApproximatorsFlow[Double](avgAgent, cmsAgent, dvAgent, hllAgent, qtAgent)

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
      val agents = new Agents[Float]("test Float approximators agents")
      val avgAgent = agents.avgAgent
      val cmsAgent = agents.cmsAgent
      val dvAgent = agents.dcaAgent
      val hllAgent = agents.hllAgent
      val qtAgent = agents.qtAgent
      val biFlow = new ApproximatorsFlow[Float](avgAgent, cmsAgent, dvAgent, hllAgent, qtAgent)

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
      val agents = new Agents[Int]("test Int approximators agents")
      val avgAgent = agents.avgAgent
      val cmsAgent = agents.cmsAgent
      val dvAgent = agents.dcaAgent
      val hllAgent = agents.hllAgent
      val qtAgent = agents.qtAgent
      val biFlow = new ApproximatorsFlow[Int](avgAgent, cmsAgent, dvAgent, hllAgent, qtAgent)

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
      val agents = new Agents[Long]("test Long approximators agents")
      val avgAgent = agents.avgAgent
      val cmsAgent = agents.cmsAgent
      val dvAgent = agents.dcaAgent
      val hllAgent = agents.hllAgent
      val qtAgent = agents.qtAgent
      val biFlow = new ApproximatorsFlow[Long](avgAgent, cmsAgent, dvAgent, hllAgent, qtAgent)

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
