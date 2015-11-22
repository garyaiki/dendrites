/**
  */
package org.gs.algebird.agent

import com.twitter.algebird._
import org.gs._
import org.gs.algebird._
import org.gs.fixtures.{ TestValuesBuilder, TrigUtils }
import org.scalatest.WordSpecLike
import org.scalatest._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global

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
        result(90).average(10.0) > meanDay90
      }
    }
    "equal the first 90 values" in {
        val old = decayedValues.agent.get().take(90)
        assert(old(89).average(10.0) > meanDay90)
    }
    "have a lower average after droping first 90" in {
      val newer = decayedValues.agent.get().drop(90)
      assert(newer(90).average(10.0) < meanDay90)
    }
  }
}
