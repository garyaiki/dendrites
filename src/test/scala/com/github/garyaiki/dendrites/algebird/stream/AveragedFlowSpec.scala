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
package com.github.garyaiki.dendrites.algebird.stream

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.twitter.algebird.AveragedValue
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import com.github.garyaiki.dendrites.aggregator.mean
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder

/**
  * @author Gary Struthers
  *
  */
class AveragedFlowSpec extends WordSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  "An AveragedValue Flow of BigDecimals" should {
    "be near its mean" in {
      val (pub, sub) = TestSource.probe[Seq[BigDecimal]]
        .via(avgFlow)
        .toMat(TestSink.probe[AveragedValue])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(bigDecimals)
      val avBD = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      avBD.count shouldBe bigDecimals.size
      val mBD = mean(bigDecimals)
      avBD.value shouldBe mBD.right.get.toDouble +- 0.005
    }
  }

  "An AveragedValue Flow of BigInts" should {
    "be near its mean" in {
      val (pub, sub) = TestSource.probe[Seq[BigInt]]
        .via(avgFlow)
        .toMat(TestSink.probe[AveragedValue])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(bigInts)
      val avBI = sub.expectNext()
      val mBI = mean(bigInts)
      avBI.count shouldBe bigInts.size
      avBI.value shouldBe mBI.right.get.toDouble +- 0.5
    }
  }

  "An AveragedValue Flow of Doubles" should {
    "be near their mean" in {
      val (pub, sub) = TestSource.probe[Seq[Double]]
        .via(avgFlow)
        .toMat(TestSink.probe[AveragedValue])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(doubles)
      val avD = sub.expectNext()
      val mD = mean(doubles)
      avD.count shouldBe doubles.size
      avD.value shouldBe mD.right.get +- 0.005
    }
  }

  "An AveragedValue Flow of Floats" should {
    "be near their mean" in {
      val (pub, sub) = TestSource.probe[Seq[Float]]
        .via(avgFlow)
        .toMat(TestSink.probe[AveragedValue])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(floats)
      val avF = sub.expectNext()
      val mF = mean(floats)
      avF.count shouldBe floats.size
      avF.value shouldBe mF.right.get.toDouble +- 0.005
    }
  }

  "An AveragedValue Flow of Ints" should {
    "be near their mean" in {
      val (pub, sub) = TestSource.probe[Seq[Int]]
        .via(avgFlow)
        .toMat(TestSink.probe[AveragedValue])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(ints)
      val avI = sub.expectNext()
      val mI = mean(ints)
      avI.count shouldBe ints.size
      avI.value shouldBe mI.right.get.toDouble +- 0.5
    }
  }

  "An AveragedValue Flow of Longs" should {
    "be near their mean" in {
      val (pub, sub) = TestSource.probe[Seq[Long]]
        .via(avgFlow)
        .toMat(TestSink.probe[AveragedValue])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(longs)
      val avL = sub.expectNext()
      val mL = mean(longs)
      avL.count shouldBe ints.size
      avL.value shouldBe mL.right.get.toDouble +- 0.5
    }
  }
}
