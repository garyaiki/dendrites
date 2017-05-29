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
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.twitter.algebird.AveragedValue
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.github.garyaiki.dendrites.aggregator.mean
import com.github.garyaiki.dendrites.algebird.avg
import com.github.garyaiki.dendrites.algebird.agent.AveragedAgent
import com.github.garyaiki.dendrites.algebird.stream.avgFlow
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder

/**
  * @author Gary Struthers
  *
  */
class AveragedAgentFlowSpec extends WordSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  val timeout = Timeout(3000 millis)

  "An AveragedAgentFlow of BigDecimals" should {
    "update its AveragedValue" in {
      val avgAgt = new AveragedAgent("test BigDecimals")
      val avgAgtFlow = new AveragedAgentFlow(avgAgt)
      val (pub, sub) = TestSource.probe[Seq[BigDecimal]]
        .via(avgFlow)
        .via(avgAgtFlow)
        .toMat(TestSink.probe[Future[AveragedValue]])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(bigDecimals)
      val updateFuture = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      val mBD = mean(bigDecimals)
      whenReady(updateFuture, timeout) { result =>
          result should equal(avg(bigDecimals))
          result.value shouldBe mBD.right.get.toDouble +- 0.005
      }
    }
  }

  "An AveragedAgentFlow of Doubles" should {
    "update its AveragedValue" in {
      val avgAgt = new AveragedAgent("test Doubles")
      val avgAgtFlow = new AveragedAgentFlow(avgAgt)
      val (pub, sub) = TestSource.probe[Seq[Double]]
        .via(avgFlow)
        .via(avgAgtFlow)
        .toMat(TestSink.probe[Future[AveragedValue]])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(doubles)
      val updateFuture = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      val mD = mean(doubles)
      whenReady(updateFuture, timeout) { result =>
        result shouldBe avg(doubles)
        result.value shouldBe mD.right.get +- 0.005
      }
    }
  }

  "An AveragedAgentFlow of Floats" should {
    "update its AveragedValue" in {
      val avgAgt = new AveragedAgent("test Floats")
      val avgAgtFlow = new AveragedAgentFlow(avgAgt)
      val (pub, sub) = TestSource.probe[Seq[Float]]
        .via(avgFlow)
        .via(avgAgtFlow)
        .toMat(TestSink.probe[Future[AveragedValue]])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(floats)
      val updateFuture = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      val mF = mean(floats)
      whenReady(updateFuture, timeout) { result =>
        result shouldBe avg(floats)
        result.value shouldBe mF.right.get.toDouble +- 0.005
      }
    }
  }

  "A composite AveragedAgentFlow of BigDecimals" should {
    "update its AveragedValue" in {
      val avgAgent = new AveragedAgent("test BigDecimals")
      val composite = AveragedAgentFlow.compositeFlow[BigDecimal](avgAgent)
      val (pub, sub) = TestSource.probe[Seq[BigDecimal]]
        .via(composite)
        .toMat(TestSink.probe[Future[AveragedValue]])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(bigDecimals)
      val updateFuture = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      val mBD = mean(bigDecimals)
      whenReady(updateFuture, timeout) { result =>
          result shouldBe avg(bigDecimals)
          result.value shouldBe mBD.right.get.toDouble +- 0.005
      }
    }
  }

  "A composite sink of AveragedAgentFlow of Doubles" should {
    "update its AveragedValue" in {
      val source = Source.single(doubles)
      val avgAgent = new AveragedAgent("test Doubles")
      val composite = AveragedAgentFlow.compositeSink[Double](avgAgent)

      source.runWith(composite)
      Thread.sleep(10) // Stream completes before agent updates
      val updateFuture = avgAgent.agent.future()
      val mD = mean(doubles)
      whenReady(updateFuture, timeout) { result =>
        result shouldBe avg(doubles)
        result.value shouldBe mD.right.get +- 0.005
      }
    }
  }
}
