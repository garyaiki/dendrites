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
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.twitter.algebird.{DecayedValue, DecayedValueMonoid}
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.gs.algebird.AlgebirdConfigurer
import org.gs.algebird.agent.DecayedValueAgent
import org.gs.algebird.stream.ZipTimeFlow
import org.gs.fixtures.TrigUtils

/**
  * @author Gary Struthers
  *
  */
class DecayedValueAgentFlowSpec extends WordSpecLike with TrigUtils {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val m = AlgebirdConfigurer.decayedValueMonoid

  val halfLife = AlgebirdConfigurer.decayedValueHalfLife
  val timeout = Timeout(3000 millis)
  val sines = genSineWave(100, 0 to 360)
  val days = Range.Double(0.0, 361.0, 1.0)
  def nextTime[T](it: Iterator[Double])(x: T): Double = it.next
  val curriedNextTime = nextTime[Double](days.iterator) _
  val meanDay90 = sines.take(90).sum / 90
  val dvAgent = new DecayedValueAgent("test90", halfLife, None)
  val dvAgtFlow = new DecayedValueAgentFlow(dvAgent)
  val (pub, sub) = TestSource.probe[Seq[Double]]
    .via(new ZipTimeFlow[Double](curriedNextTime))
    .via(dvAgtFlow)
    .toMat(TestSink.probe[Future[Seq[DecayedValue]]])(Keep.both)
    .run()
  sub.request(1)
  pub.sendNext(sines)
  val updateFuture = sub.expectNext()
  pub.sendComplete()
  sub.expectComplete()
    
  "A DecayedValueAgentFlow of value/time doubles" should {
    "exceed the mean for 1st 90 values" in {
      whenReady(updateFuture, timeout) {result => result(90).average(halfLife) > meanDay90}
    }
  }

  it should {
    "be less than the mean for the first 180" in {
      whenReady(updateFuture, timeout) { result =>
        result(180).average(halfLife) should be < sines.take(180).sum / 180
      }
    }
  }

  it should {
    "be less than the mean for the first 270" in {
      whenReady(updateFuture, timeout) { result =>
        result(270).average(halfLife) should be < sines.take(270).sum / 270
      }
    }
  }

  it should {
    "be less than the mean for all 3600" in {

      whenReady(updateFuture, timeout) { result =>
        result(360).average(halfLife) should be < sines.take(360).sum / 360
      }
    }
  }

  "A composite DecayedValueAgentFlow of value/time doubles" should {
    "exceed the mean for 1st 90 values" in {
        val dvAgent = new DecayedValueAgent("test90", halfLife, None)
        val composite = DecayedValueAgentFlow.compositeFlow[Double](dvAgent,
            DecayedValueAgentFlow.nowMillis)
        val (pub, sub) = TestSource.probe[Seq[Double]]
          .via(composite)
          .toMat(TestSink.probe[Future[Seq[DecayedValue]]])(Keep.both)
          .run()
        sub.request(1)
        pub.sendNext(sines)
        val updateFuture = sub.expectNext()
        pub.sendComplete()
        sub.expectComplete()
        whenReady(updateFuture, timeout) {  result =>
          result(90).average(halfLife) > meanDay90
        }
    }
  }

  "A composite sink of DecayedValueAgentFlow of value/time doubles" should {
    "exceed the mean for 1st 90 values" in {
        val dvAgent = new DecayedValueAgent("test90", halfLife, None)
        val source = Source.single(sines)
        val composite = DecayedValueAgentFlow.compositeSink[Double](dvAgent,
            DecayedValueAgentFlow.nowMillis)
        source.runWith(composite)
        Thread.sleep(10)//Stream completes before agent updates
         
        val updateFuture = dvAgent.agent.future()
        whenReady(updateFuture, timeout) {  result =>
          result(90).average(halfLife) > meanDay90
        }
    }
  }
}
