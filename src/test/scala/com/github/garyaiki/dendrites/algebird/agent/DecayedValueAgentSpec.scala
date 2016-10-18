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
package com.github.garyaiki.dendrites.algebird.agent

import com.twitter.algebird._
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.garyaiki.dendrites.fixtures.TrigUtils

/** @author garystruthers
  *
  */
class DecayedValueAgentSpec extends WordSpecLike with TrigUtils {
  implicit val m = DecayedValueMonoid(0.001)
  val timeout = Timeout(3000 millis)
  val sines = genSineWave(100, 0 to 360)
  val days = Range.Double(0.0, 361.0, 1.0)
  val meanDay90 = sines.take(90).sum / 90
  val sinesZip = sines.zip(days)

  "A DecayedValueAgent average with halfLife 10.0" should {
    val decayedValues = new DecayedValueAgent("test90", 10.0, None)
    val updateFuture = decayedValues.alter(sinesZip)
    "exceed the mean at 90º" in {
      whenReady(updateFuture, timeout) { result =>
        result(90).average(10.0) should be > meanDay90
      }
    }

    "equal the first 90 values" in {
        val old = decayedValues.agent.get().take(90)
        old(89).average(10.0) should be > meanDay90
    }

    "have a lower average after droping first 90" in {
      val newer = decayedValues.agent.get().drop(90)
      newer(90).average(10.0) should be < meanDay90
    }
  }
}
