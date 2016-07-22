/** Copyright 2016 Gary Struthers

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
package org.gs.algebird.agent.stream


import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Source, Sink}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.twitter.algebird.{QTree, QTreeSemigroup}
import com.twitter.algebird.CMSHasherImplicits._
import com.twitter.algebird._
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.Matchers._
import org.scalatest._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.reflect.runtime.universe.TypeTag
import org.gs.aggregator.mean
import org.gs.aggregator._
import org.gs.algebird._
import org.gs.algebird.agent.{AveragedAgent, CountMinSketchAgent, DecayedValueAgent, HyperLogLogAgent, QTreeAgent}
import org.gs.algebird.typeclasses.QTreeLike
import org.gs.fixtures.TestValuesBuilder

/**
  * @author Gary Struthers
  *
  */
class ParallelApproximatorsSinkSpec extends WordSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val halfLife = AlgebirdConfigurer.decayedValueHalfLife
  implicit val m = AlgebirdConfigurer.decayedValueMonoid
  implicit val ag = AlgebirdConfigurer.hyperLogLogAgggregator
  implicit val monoid = AlgebirdConfigurer.hyperLogLogMonoid
  val qTreeLevel = AlgebirdConfigurer.qTreeLevel
  val timeout = Timeout(3000 millis)
  
  "A composite sink of BigDecimals ParallelApproximators" should {
    "update all agents and return their latest values" in {
      implicit val qtBDSemigroup = new QTreeSemigroup[BigDecimal](qTreeLevel)

      val avgAgent = new AveragedAgent("test approximators Averaged Value Agent")
      val cmsAgent = new CountMinSketchAgent[BigDecimal]("test approximators CountMinSketch Agent")
      val dvAgent = new DecayedValueAgent("test approximators DecayedValue Agent", halfLife)
      val hllAgent = new HyperLogLogAgent("test approximators HyperLogLog Agent")
      val qtAgent = new QTreeAgent[BigDecimal]("test approximators QTree Agent")
      val source = Source.single(bigDecimals)
      val composite = ParallelApproximators.compositeSink[BigDecimal](avgAgent,
        cmsAgent,
        dvAgent,
        hllAgent,
        qtAgent,
        DecayedValueAgentFlow.nowMillis)

      source.runWith(composite)
      Thread.sleep(60)//Stream completes before agent updates

      val updateAvgFuture = avgAgent.agent.future()
      whenReady(updateAvgFuture, timeout) { result =>
        val count = result.count
        count should equal(bigDecimals.size)
        val dValue: Double = result.value
        val mD = mean(bigDecimals)
        val expectMean = mD.right.get.toDouble
        dValue should be (expectMean  +- 0.1)
      }

      val updateCMSFuture = cmsAgent.agent.future()
      whenReady(updateCMSFuture, timeout) { result =>
        val totalCount = result.totalCount
        totalCount should equal(longs.size)
      }

      val updateDVFuture = dvAgent.agent.future()
      whenReady(updateDVFuture, timeout) {  result =>
        val size = result.size
        size should be (bigDecimals.size +- 2)
      }

      val updateHLLFuture = hllAgent.agent.future()
      whenReady(updateHLLFuture, timeout) { result =>
        val estimatedSize = result.estimatedSize
        estimatedSize should be(longs.distinct.size.toDouble +- 0.09)
      }

		  val updateQTFuture = qtAgent.agent.future()
		  whenReady(updateQTFuture, timeout) { result =>
		    result.count should equal(bigDecimals.size)
		  }
    }
  }
  
  "A BigInt ParallelApproximators" should {
    "update all agents and return their latest values" in {
      implicit val qtBDSemigroup = new QTreeSemigroup[BigInt](qTreeLevel)

      val avgAgent = new AveragedAgent("test approximators Averaged Value Agent")
      val cmsAgent = new CountMinSketchAgent[BigInt]("test approximators CountMinSketch Agent")
      val dvAgent = new DecayedValueAgent("test approximators DecayedValue Agent", halfLife)
      val hllAgent = new HyperLogLogAgent("test approximators HyperLogLog Agent")
      val qtAgent = new QTreeAgent[BigInt]("test approximators QTree Agent")
      val source = Source.single(bigInts)
      val composite = ParallelApproximators.compositeSink[BigInt](avgAgent,
        cmsAgent,
        dvAgent,
        hllAgent,
        qtAgent,
        DecayedValueAgentFlow.nowMillis)

      source.runWith(composite)
      Thread.sleep(30)//Stream completes before agent updates
      /*
      val ffg = Flow.fromGraph(composite)
      val (pub, sub) = TestSource.probe[Seq[BigInt]]
        .via(ffg)
        .toMat(TestSink.probe[(Future[AveragedValue],
            Future[CMS[BigInt]],
            Future[Seq[com.twitter.algebird.DecayedValue]],
            Future[HLL],
            Future[QTree[BigInt]])])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(bigInts)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      source.via(ffg).runWith(Sink.ignore)
*/
      val updateAvgFuture = avgAgent.agent.future()
      whenReady(updateAvgFuture, timeout) { result =>
        val count = result.count
        count should equal(bigInts.size)
        val dValue: Double = result.value
        val mD = mean(bigInts)
        val expectMean = mD.right.get.toDouble
        dValue should be (expectMean  +- 0.5)
      }

      val updateCMSFuture = cmsAgent.agent.future()
      whenReady(updateCMSFuture, timeout) { result =>
        val totalCount = result.totalCount
        totalCount should equal(longs.size)
      }

      val updateDVFuture = dvAgent.agent.future()
      whenReady(updateDVFuture, timeout) {  result =>
        val size = result.size
        size should be (bigInts.size +- 2)
      }

      val updateHLLFuture = hllAgent.agent.future()
      whenReady(updateHLLFuture, timeout) { result =>
        val estimatedSize = result.estimatedSize
        estimatedSize should be(longs.distinct.size.toDouble +- 0.09)
      }

		  val updateQTFuture = qtAgent.agent.future()
		  whenReady(updateQTFuture, timeout) { result =>
		    result.count should equal(bigInts.size)
		  }
    }
  }
  
  "A Double ParallelApproximators" should {
    "update all agents and return their latest values" in {
      implicit val qtBDSemigroup = new QTreeSemigroup[Double](qTreeLevel)

      val avgAgent = new AveragedAgent("test approximators Averaged Value Agent")
      val cmsAgent = new CountMinSketchAgent[Double]("test approximators CountMinSketch Agent")
      val dvAgent = new DecayedValueAgent("test approximators DecayedValue Agent", halfLife)
      val hllAgent = new HyperLogLogAgent("test approximators HyperLogLog Agent")
      val qtAgent = new QTreeAgent[Double]("test approximators QTree Agent")
      val source = Source.single(doubles)
      val composite = ParallelApproximators.compositeSink[Double](avgAgent,
        cmsAgent,
        dvAgent,
        hllAgent,
        qtAgent,
        DecayedValueAgentFlow.nowMillis)

      source.runWith(composite)
      Thread.sleep(30)//Stream completes before agent updates
      /*
      val ffg = Flow.fromGraph(composite)
      val (pub, sub) = TestSource.probe[Seq[Double]]
        .via(ffg)
        .toMat(TestSink.probe[(Future[AveragedValue],
            Future[CMS[Double]],
            Future[Seq[com.twitter.algebird.DecayedValue]],
            Future[HLL],
            Future[QTree[Double]])])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(doubles)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      //source.via(ffg).runWith(Sink.ignore)*/

      val updateAvgFuture = avgAgent.agent.future()
      whenReady(updateAvgFuture, timeout) { result =>
        val count = result.count
        count should equal(doubles.size)
        val dValue: Double = result.value
        val mD = mean(doubles)
        val expectMean = mD.right.get
        dValue should be (expectMean  +- 0.5)
      }

      val updateCMSFuture = cmsAgent.agent.future()
      whenReady(updateCMSFuture, timeout) { result =>
        val totalCount = result.totalCount
        totalCount should equal(longs.size)
      }

      val updateDVFuture = dvAgent.agent.future()
      whenReady(updateDVFuture, timeout) {  result =>
        val size = result.size
        size should be (bigInts.size +- 2)
      }

      val updateHLLFuture = hllAgent.agent.future()
      whenReady(updateHLLFuture, timeout) { result =>
        val estimatedSize = result.estimatedSize
        estimatedSize should be(3.0 +- 0.09)//@FIXME
      }

		  val updateQTFuture = qtAgent.agent.future()
		  whenReady(updateQTFuture, timeout) { result =>
		    result.count should equal(doubles.size)
		  }
    }
  }
  
  "A Float ParallelApproximators" should {
    "update all agents and return their latest values" in {
      implicit val qtBDSemigroup = new QTreeSemigroup[Float](qTreeLevel)

      val avgAgent = new AveragedAgent("test approximators Averaged Value Agent")
      val cmsAgent = new CountMinSketchAgent[Float]("test approximators CountMinSketch Agent")
      val dvAgent = new DecayedValueAgent("test approximators DecayedValue Agent", halfLife)
      val hllAgent = new HyperLogLogAgent("test approximators HyperLogLog Agent")
      val qtAgent = new QTreeAgent[Float]("test approximators QTree Agent")
      val source = Source.single(floats)
      val composite = ParallelApproximators.compositeSink[Float](avgAgent,
        cmsAgent,
        dvAgent,
        hllAgent,
        qtAgent,
        DecayedValueAgentFlow.nowMillis)

      source.runWith(composite)
      Thread.sleep(30)//Stream completes before agent updates
      /*
      val ffg = Flow.fromGraph(composite)
      val (pub, sub) = TestSource.probe[Seq[Float]]
        .via(ffg)
        .toMat(TestSink.probe[(Future[AveragedValue],
            Future[CMS[Float]],
            Future[Seq[com.twitter.algebird.DecayedValue]],
            Future[HLL],
            Future[QTree[Float]])])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(floats)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      //source.via(ffg).runWith(Sink.ignore)*/

      val updateAvgFuture = avgAgent.agent.future()
      whenReady(updateAvgFuture, timeout) { result =>
        val count = result.count
        count should equal(ints.size)
        val dValue: Double = result.value
        val mD = mean(ints)
        val expectMean = mD.right.get.toDouble
        dValue should be (expectMean  +- 12.0)
      }

      val updateCMSFuture = cmsAgent.agent.future()
      whenReady(updateCMSFuture, timeout) { result =>
        val totalCount = result.totalCount
        totalCount should equal(longs.size)
      }

      val updateDVFuture = dvAgent.agent.future()
      whenReady(updateDVFuture, timeout) {  result =>
        val size = result.size
        size should be (ints.size +- 2)
      }

      val updateHLLFuture = hllAgent.agent.future()
      whenReady(updateHLLFuture, timeout) { result =>
        val estimatedSize = result.estimatedSize
        estimatedSize should be(longs.distinct.size.toDouble +- 0.09)
      }

		  val updateQTFuture = qtAgent.agent.future()
		  whenReady(updateQTFuture, timeout) { result =>
		    result.count should equal(floats.size)
		  }
    }
  }
  
  "A Int ParallelApproximators" should {
    "update all agents and return their latest values" in {
      implicit val qtBDSemigroup = new QTreeSemigroup[Int](qTreeLevel)

      val avgAgent = new AveragedAgent("test approximators Averaged Value Agent")
      val cmsAgent = new CountMinSketchAgent[Int]("test approximators CountMinSketch Agent")
      val dvAgent = new DecayedValueAgent("test approximators DecayedValue Agent", halfLife)
      val hllAgent = new HyperLogLogAgent("test approximators HyperLogLog Agent")
      val qtAgent = new QTreeAgent[Int]("test approximators QTree Agent")
      val source = Source.single(ints)
      val composite = ParallelApproximators.compositeSink[Int](avgAgent,
        cmsAgent,
        dvAgent,
        hllAgent,
        qtAgent,
        DecayedValueAgentFlow.nowMillis)

      source.runWith(composite)
      Thread.sleep(30)//Stream completes before agent updates
/*
      val ffg = Flow.fromGraph(composite)
      val (pub, sub) = TestSource.probe[Seq[Int]]
        .via(ffg)
        .toMat(TestSink.probe[(Future[AveragedValue],
            Future[CMS[Int]],
            Future[Seq[com.twitter.algebird.DecayedValue]],
            Future[HLL],
            Future[QTree[Int]])])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(ints)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      //source.via(ffg).runWith(Sink.ignore)*/

      val updateAvgFuture = avgAgent.agent.future()
      whenReady(updateAvgFuture, timeout) { result =>
        val count = result.count
        count should equal(ints.size)
        val dValue: Double = result.value
        val mD = mean(ints)
        val expectMean = mD.right.get.toDouble
        dValue should be (expectMean  +- 0.5)
      }

      val updateCMSFuture = cmsAgent.agent.future()
      whenReady(updateCMSFuture, timeout) { result =>
        val totalCount = result.totalCount
        totalCount should equal(longs.size)
      }

      val updateDVFuture = dvAgent.agent.future()
      whenReady(updateDVFuture, timeout) {  result =>
        val size = result.size
        size should be (floats.size +- 2)
      }

      val updateHLLFuture = hllAgent.agent.future()
      whenReady(updateHLLFuture, timeout) { result =>
        val estimatedSize = result.estimatedSize
        estimatedSize should be(longs.distinct.size.toDouble +- 0.09)
      }

		  val updateQTFuture = qtAgent.agent.future()
		  whenReady(updateQTFuture, timeout) { result =>
		    result.count should equal(floats.size)
		  }
    }
  }
  
  "A Long ParallelApproximators" should {
    "update all agents and return their latest values" in {
      implicit val qtBDSemigroup = new QTreeSemigroup[Long](qTreeLevel)

      val avgAgent = new AveragedAgent("test approximators Averaged Value Agent")
      val cmsAgent = new CountMinSketchAgent[Long]("test approximators CountMinSketch Agent")
      val dvAgent = new DecayedValueAgent("test approximators DecayedValue Agent", halfLife)
      val hllAgent = new HyperLogLogAgent("test approximators HyperLogLog Agent")
      val qtAgent = new QTreeAgent[Long]("test approximators QTree Agent")
      val source = Source.single(longs)
      val composite = ParallelApproximators.compositeSink[Long](avgAgent,
        cmsAgent,
        dvAgent,
        hllAgent,
        qtAgent,
        DecayedValueAgentFlow.nowMillis)

      source.runWith(composite)
      Thread.sleep(30)//Stream completes before agent updates

/*      val ffg = Flow.fromGraph(composite)
      val (pub, sub) = TestSource.probe[Seq[Long]]
        .via(ffg)
        .toMat(TestSink.probe[(Future[AveragedValue],
            Future[CMS[Long]],
            Future[Seq[com.twitter.algebird.DecayedValue]],
            Future[HLL],
            Future[QTree[Long]])])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(longs)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      //source.via(ffg).runWith(Sink.ignore)*/

      val updateAvgFuture = avgAgent.agent.future()
      whenReady(updateAvgFuture, timeout) { result =>
        val count = result.count
        count should equal(longs.size)
        val dValue: Double = result.value
        val mD = mean(longs)
        val expectMean = mD.right.get.toDouble
        dValue should be (expectMean  +- 0.5)
      }

      val updateCMSFuture = cmsAgent.agent.future()
      whenReady(updateCMSFuture, timeout) { result =>
        val totalCount = result.totalCount
        totalCount should equal(longs.size)
      }

      val updateDVFuture = dvAgent.agent.future()
      whenReady(updateDVFuture, timeout) {  result =>
        val size = result.size
        size should be (longs.size +- 2)
      }

      val updateHLLFuture = hllAgent.agent.future()
      whenReady(updateHLLFuture, timeout) { result =>
        val estimatedSize = result.estimatedSize
        estimatedSize should be(longs.distinct.size.toDouble +- 0.09)
      }

		  val updateQTFuture = qtAgent.agent.future()
		  whenReady(updateQTFuture, timeout) { result =>
		    result.count should equal(longs.size)
		  }
    }
  }
}
