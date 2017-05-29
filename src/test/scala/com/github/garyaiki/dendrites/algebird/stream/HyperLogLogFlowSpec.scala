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
import akka.stream.scaladsl.{Flow, Keep}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.twitter.algebird.{Approximate, HLL}
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import com.github.garyaiki.dendrites.algebird.{AlgebirdConfigurer, createHLL}
import com.github.garyaiki.dendrites.algebird.typeclasses.HyperLogLogLike
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder

/**
  *
  * @author Gary Struthers
  *
  */
class HyperLogLogFlowSpec extends WordSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val ag = AlgebirdConfigurer.hyperLogLogAgggregator
  val hll = createHLL(ints)
  val hll2 = createHLL(ints2)
  val hllV = Vector(hll, hll2)

  "A HyperLogLog Flow" should {
    "estimate number of distinct integers from a Sequence of Int" in {
      val (pub, sub) = TestSource.probe[HLL]
        .via(estSizeFlow)
        .toMat(TestSink.probe[Double])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(hll)
      val size = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      size shouldBe ints.distinct.size.toDouble +- 0.09
    }
  }

  it should {
    "map an HLL to an Approximate" in {
      val (pub, sub) = TestSource.probe[HLL]
        .via(toApproximate)
        .toMat(TestSink.probe[Approximate[Long]])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(hll)
      val approx = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      approx.estimate shouldBe ints.size
    }
  }

  it should {
    "map a Sequence of HLL to a Sequence of Approximate" in {
      val (pub, sub) = TestSource.probe[Seq[HLL]]
        .via(toApproximates)
        .toMat(TestSink.probe[Seq[Approximate[Long]]])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(hllV)
      val approxs = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      val sum = approxs.reduce(_ + _)
      sum.estimate shouldBe ints.size + ints2.size
    }
  }

  it should {
    "sum a Sequence of HLL to an Approximate" in {
      val (pub, sub) = TestSource.probe[Seq[HLL]]
        .via(sumHLLs)
        .toMat(TestSink.probe[Approximate[Long]])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(hllV)
      val sum = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      sum.estimate shouldBe ints.size + ints2.size
    }
  }
}
