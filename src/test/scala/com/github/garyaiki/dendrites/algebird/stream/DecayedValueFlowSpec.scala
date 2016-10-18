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
package com.github.garyaiki.dendrites.algebird.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.twitter.algebird.DecayedValue
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import scala.collection.immutable.Range
import com.github.garyaiki.dendrites.algebird.{AlgebirdConfigurer, toDecayedValues}
import com.github.garyaiki.dendrites.fixtures.TrigUtils

/**
  *
  * @author Gary Struthers
  *
  */
class DecayedValueFlowSpec extends WordSpecLike with TrigUtils {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val monoid = AlgebirdConfigurer.decayedValueMonoid
  val sines = genSineWave(100, 0 to 360)
  val days = Range.Double(0.0, 361.0, 1.0)
  val sinesZip = sines.zip(days)
  val decayedValues: Flow[Seq[(Double, Double)], Seq[DecayedValue], NotUsed] =
    Flow[Seq[(Double, Double)]].map(toDecayedValues(10.0) _)
  val (pub, sub) = TestSource.probe[Seq[(Double, Double)]]
    .via(decayedValues)
    .toMat(TestSink.probe[Seq[DecayedValue]])(Keep.both).run()
  sub.request(1)
  pub.sendNext(sinesZip)
  val dvs = sub.expectNext()
  pub.sendComplete()
  sub.expectComplete()

  "A DecayedValue average with halfLife 10.0" should {
    "exceed the mean at 90º" in { dvs(90).average(10.0) should be > sines.take(90).sum / 90 }
  }

  it should {
    "be < the mean at 180º" in { dvs(180).average(10.0) should be < sines.take(180).sum / 180 }
  }

  it should {
    "be < the mean at 270º" in { dvs(270).average(10.0) should be < sines.take(270).sum / 270 }
  }

  it should {
    "be < mean at 360º" in { dvs(360).average(10.0) should be < sines.take(360).sum / 360 }
  }
}
