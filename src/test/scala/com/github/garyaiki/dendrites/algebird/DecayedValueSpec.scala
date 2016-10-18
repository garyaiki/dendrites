/**
  */
package com.github.garyaiki.dendrites.algebird

import com.twitter.algebird._
import com.github.garyaiki.dendrites._
import com.github.garyaiki.dendrites.algebird._
import com.github.garyaiki.dendrites.fixtures.{ TestValuesBuilder, TrigUtils }
import org.scalatest.FlatSpecLike

/** @author garystruthers
  *
  */
class DecayedValueSpec extends FlatSpecLike with TrigUtils {
  implicit val m = DecayedValueMonoid(0.001)
  val sines = genSineWave(100, 0 to 360)
  val days = Range.Double(0.0, 361.0, 1.0)
  val meanDay0 = sines.take(0).sum / 1
  val meanDay90 = sines.take(90).sum / 90
  val meanDay180 = sines.take(180).sum / 180
  val meanDay270 = sines.take(270).sum / 270
  val meanDay360 = sines.take(360).sum / 360
  val sinesZip = sines.zip(days)
  val decayedValuesten = toDecayedValues(10.0, None)(sinesZip)

  "A DecayedValue average with halfLife 10.0" should "exceed the mean at 90º" in {
    assert(decayedValuesten(90).average(10.0) > meanDay90)
  }

  it should "be less than the mean at 180º" in {
    assert(decayedValuesten(180).average(10.0) < meanDay180)
  }

  it should "be less than the mean at 270º" in {
    assert(decayedValuesten(270).average(10.0) < meanDay270)
  }

  it should "be less than mean at 360º" in {
    assert(decayedValuesten(360).average(10.0) < meanDay360)
  }
  
  "A DecayedValue moving average with halfLife 10.0" should "exceed the decayed average at 90º" in {
    assert(decayedValuesten(90).averageFrom(10.0, 80.0, 90.0) > decayedValuesten(90).average(10.0))
  }

  it should "exceed the decayed average at 180º" in {
    assert(decayedValuesten(180).averageFrom(10.0, 170.0, 180.0) >
      decayedValuesten(180).average(10.0))
  }

  it should "be less than the decayed average at 270º" in {
    assert(decayedValuesten(270).averageFrom(10.0, 260.0, 270.0) <
      decayedValuesten(270).average(10.0))
  }

  it should "be less than the decayed average at 360º" in {
    assert(decayedValuesten(360).averageFrom(10.0, 350.0, 360.0) <
      decayedValuesten(360).average(10.0))
  }
  
  "A DecayedValue discrete average with halfLife 9.0" should "exceed the average at 90º" in {
    assert(decayedValuesten(90).discreteAverage(9.0) > decayedValuesten(90).average(10.0))
  }


  it should "exceed the average at 180º" in {
    assert(decayedValuesten(180).discreteAverage(9.0) > decayedValuesten(180).average(10.0))
  }


  it should "be less than the average at 270º" in {
    assert(decayedValuesten(270).discreteAverage(9.0) < decayedValuesten(270).average(10.0))
  }



  it should "be less than the average at 360º" in {
    assert(decayedValuesten(360).discreteAverage(9.0) < decayedValuesten(360).average(10.0))
  }
}
