/**
  */
package org.gs.algebird

import com.twitter.algebird._
import org.scalatest.FlatSpecLike
//import org.scalatest.Matchers._
import org.gs._
import org.gs.fixtures.{ CaseClassLike, NScalaTimeUtils, TestValuesBuilder, TrigUtils }
import org.gs.algebird._

/** @author garystruthers
  *
  */
class DecayedValueSpec extends FlatSpecLike with TrigUtils {
  implicit val m = DecayedValueMonoid(0.001)
  val sines = genSineWave(100)
  val days = Range.Double(0.0, 361.0, 1.0)
  val meanDay0 = sines.take(0).sum / 1
  val meanDay90 = sines.take(90).sum / 90
  val meanDay180 = sines.take(180).sum / 180
  val meanDay270 = sines.take(270).sum /270
  val meanDay360 = sines.take(360).sum /360  
  val sinesZip = sines.zip(days)
  val decayedValuesten = toDecayedValues(sinesZip, 10.0, None)

  "A DecayedValue" should "average greater than mean at 90º and halfLife 10.0" in {
    assert(decayedValuesten(90).average(10.0) > meanDay90)
  }
  
  it should "averageFrom start:80º end 90º greater than average at 90º and halfLife 10.0" in {
    assert(decayedValuesten(90).averageFrom(10.0, 80.0, 90.0) > decayedValuesten(90).average(10.0))
  }

  it should "discreteAverage less than average at 90º and halfLife 10.0" in {
    assert(decayedValuesten(90).discreteAverage(10.0) < decayedValuesten(90).average(10.0))
  }

  it should "average less than mean at 180º and halfLife 10.0" in {
    assert(decayedValuesten(180).average(10.0) < meanDay180)
  }
  
  it should "averageFrom start:170º end 180º greater than average at 180º and halfLife 10.0" in {
    assert(decayedValuesten(180).averageFrom(10.0, 170.0, 180.0) >
      decayedValuesten(180).average(10.0))
  }

  it should "discreteAverage less than average at 180º and halfLife 10.0" in {
    assert(decayedValuesten(180).discreteAverage(10.0) < decayedValuesten(180).average(10.0))
  }

  it should "average less than mean at 270º and halfLife 10.0" in {
    assert(decayedValuesten(270).average(10.0) < meanDay270)
  }
  
  it should "averageFrom start:260º end 270º less than average at 270º and halfLife 10.0" in {
    assert(decayedValuesten(270).averageFrom(10.0, 260.0, 270.0) <
      decayedValuesten(270).average(10.0))
  }

  it should "discreteAverage greater than average at 270º and halfLife 10.0" in {
    assert(decayedValuesten(270).discreteAverage(10.0) > decayedValuesten(270).average(10.0))
  }

  it should "average less than mean at 360º and halfLife 10.0" in {
    assert(decayedValuesten(360).average(10.0) < meanDay360)
  }
  
  it should "averageFrom start:350º end 360º less than average at 360º and halfLife 10.0" in {
    assert(decayedValuesten(360).averageFrom(10.0, 350.0, 360.0) <
      decayedValuesten(360).average(10.0))
  }

  it should "discreteAverage greater than average at 360º and halfLife 10.0" in {
    assert(decayedValuesten(360).discreteAverage(10.0) > decayedValuesten(360).average(10.0))
  }
}
