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
import org.scalatest.{Matchers, WordSpecLike}
import com.github.garyaiki.dendrites.algebird.min
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder
import com.github.garyaiki.dendrites.stream.{collectRightFlow, flattenFlow}

/**
  *
  * @author Gary Struthers
  */
class MinFlowSpec extends WordSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  "A Sequence of BigInt" should {
    "return its Min" in {
      val (pub, sub) = TestSource.probe[Seq[BigInt]]
        .via(minFlow)
        .toMat(TestSink.probe[BigInt])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(bigInts)
      sub.expectNext(bigInts.min)
      pub.sendComplete()
      sub.expectComplete()
      min(bigInts) === bigInts.min
    }
  }

  "A Sequence of Option[Double]" should {
    "return its Min" in {
      val (pub, sub) = TestSource.probe[Seq[Option[Double]]]
        .via(flattenFlow)
        .via(minFlow)
        .toMat(TestSink.probe[Double])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(optDoubles)
      sub.expectNext(optDoubles.flatten.min)
      pub.sendComplete()
      sub.expectComplete()
    }
  }

  "A Sequence of Either[String, Float]" should {
    "return its Min" in {
      val (pub, sub) = TestSource.probe[Seq[Either[String, Float]]]
        .via(collectRightFlow)
        .via(minFlow)
        .toMat(TestSink.probe[Float])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(eithFloats)
      sub.expectNext(floats.min)
      pub.sendComplete()
      sub.expectComplete()
    }
  }
}
