/**
  */
package org.gs.algebird

import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import org.gs.fixtures.{ CaseClassLike, NScalaTimeUtils, TestValuesBuilder, TrigUtils }
import org.gs.algebird._
import org.gs._
import com.twitter.algebird._

/** @author garystruthers
  *
  */
class DecayedValueSpec extends FlatSpecLike with TrigUtils {
  implicit val m = DecayedValueMonoid(0.001)
  val sines = sinePeriod(100)
  val days = Range.Double(0.0, 361.0, 1.0)
  info(s"days 0:${days(0).toString()}")
  info(s"days 90:${days(90).toString()}")
  info(s"days 180:${days(180).toString()}")
  info(s"days 270:${days(270).toString()}")
  info(s"days 360:${days(360).toString()}")
  info(s"1 day:${days(1) - days(0)}")
  info(s"360 days:${days(360) - days(0)}")
  val mr = 0 until 10
  for (i <- mr) {
    val ltHF = sines.take(i)
    val mean = ltHF.sum / (i + 1)
    info(s"mean i:$i $mean")
  }
  val decayedValuesten = toDecayedValues(sines.zip(days), 10.0, None)
  info(s"decayedValuesten 0:${decayedValuesten(0).average(10.0)}")
  info(s"decayedValuesten 1:${decayedValuesten(1).average(10.0)}")
  info(s"decayedValuesten 2:${decayedValuesten(2).average(10.0)}")
  info(s"decayedValuesten 3:${decayedValuesten(3).average(10.0)}")
  info(s"decayedValuesten 4:${decayedValuesten(4).average(10.0)}")
  info(s"decayedValuesten 5:${decayedValuesten(5).average(10.0)}")
  info(s"decayedValuesten 6:${decayedValuesten(6).average(10.0)}")
  info(s"decayedValuesten 7:${decayedValuesten(7).average(10.0)}")
  info(s"decayedValuesten 8:${decayedValuesten(8).average(10.0)}")
  info(s"decayedValuesten 9:${decayedValuesten(9).average(10.0)}")
  info(s"decayedValuesten 10:${decayedValuesten(10).average(10.0)}")
  info(s"decayedValuesten 11:${decayedValuesten(11).average(10.0)}")
  info(s"decayedValuesten 90:${decayedValuesten(90).average(10.0)}")
  info(s"decayedValuesten 180:${decayedValuesten(180).average(10.0)}")
  info(s"decayedValuesten 270:${decayedValuesten(270).average(10.0)}")
  info(s"decayedValuesten 360:${decayedValuesten(360).average(10.0)}")

  val decayedValues20 = toDecayedValues(sines.zip(days), 20.0, None)
  info(s"decayedValues20 0:${decayedValues20(0).average(20.0)}")
  info(s"decayedValues20 90:${decayedValues20(90).average(20.0)}")
  info(s"decayedValues20 180:${decayedValues20(180).average(20.0)}")
  info(s"decayedValues20 270:${decayedValues20(270).average(20.0)}")
  info(s"decayedValues20 360:${decayedValues20(360).average(20.0)}")

  val decayedValues30 = toDecayedValues(sines.zip(days), 30.0, None)
  info(s"decayedValues30 0:${decayedValues30(0).average(30.0)}")
  info(s"decayedValues30 90:${decayedValues30(90).average(30.0)}")
  info(s"decayedValues30 180:${decayedValues30(180).average(30.0)}")
  info(s"decayedValues30 270:${decayedValues30(270).average(30.0)}")
  info(s"decayedValues30 360:${decayedValues30(360).average(30.0)}")

  val decayedValues40 = toDecayedValues(sines.zip(days), 40.0, None)
  info(s"decayedValues40 0:${decayedValues40(0).average(40.0)}")
  info(s"decayedValues40 90:${decayedValues40(90).average(40.0)}")
  info(s"decayedValues40 180:${decayedValues40(180).average(40.0)}")
  info(s"decayedValues40 270:${decayedValues40(270).average(40.0)}")
  info(s"decayedValues40 360:${decayedValues40(360).average(40.0)}")

  val decayedValueshundred = toDecayedValues(sines.zip(days), 100.0, None)
  info(s"decayedValueshundred 0:${decayedValueshundred(0).average(100.0)}")
  info(s"decayedValueshundred 90:${decayedValueshundred(90).average(100.0)}")
  info(s"decayedValueshundred 180:${decayedValueshundred(180).average(100.0)}")
  info(s"decayedValueshundred 270:${decayedValueshundred(270).average(100.0)}")
  info(s"decayedValueshundred 360:${decayedValueshundred(360).average(100.0)}")

  val decayedValues200 = toDecayedValues(sines.zip(days), 200.0, None)
  info(s"decayedValuesthousand 0:${decayedValues200(0).average(200.0)}")
  info(s"decayedValuesthousand 90:${decayedValues200(90).average(200.0)}")
  info(s"decayedValuesthousand 180:${decayedValues200(180).average(200.0)}")
  info(s"decayedValuesthousand 270:${decayedValues200(270).average(200.0)}")
  info(s"decayedValuesthousand 360:${decayedValues200(360).average(200.0)}")

  "A DecayedValue applied to a sinewave and halfLife 10.0" should " over a year" in {

    val halfLife: Double = 10.0
    val decayedValues = toDecayedValues(sines.zip(days), halfLife, None)

    assert(decayedValues(0).value === 0.0)
    assert(decayedValues(90).value === (100.0 +- 0.02))
    assert(decayedValues(180).value === (0.0 +- 1.75))
    assert(decayedValues(270).value === (-100.0 +- 0.02))
    assert(decayedValues(360).value === (0.0 +- 1.75))

    assert(decayedValues(0).scaledTime === 0.0)
    assert(decayedValues(90).scaledTime === (100.0 +- 0.02))
    assert(decayedValues(180).scaledTime === (0.0 +- 1.75))
    assert(decayedValues(270).scaledTime === (-100.0 +- 0.02))
    assert(decayedValues(360).scaledTime === (0.0 +- 1.75))
  }

  "A DecayedValue applied to a sinewave and halfLife 100.0" should " over a year" in {

    val halfLife: Double = 100.0
    val decayedValues = toDecayedValues(sines.zip(days), halfLife, None)
    assert(decayedValues(0).value === 0.0)
    assert(decayedValues(90).value === (100.0 +- 0.02))
    assert(decayedValues(180).value === (0.0 +- 1.75))
    assert(decayedValues(270).value === (-100.0 +- 0.02))
    assert(decayedValues(360).value === (0.0 +- 1.75))
  }

  "A DecayedValue applied to a sinewave and halfLife 1.0" should " over a year" in {

    val halfLife: Double = 1.0
    val decayedValues = toDecayedValues(sines.zip(days), halfLife, None)
    assert(decayedValues(0).value === 0.0)
    assert(decayedValues(90).value === (100.0 +- 0.02))
    assert(decayedValues(180).value === (0.0 +- 1.75))
    assert(decayedValues(270).value === (-100.0 +- 0.02))
    assert(decayedValues(360).value === (0.0 +- 1.75))
  }

  val slopes = for (i <- 0 to 100) yield i.toDouble
  val hundredDays = Range.Double(0.0, 101.0, 1.0)

  "A DecayedValue applied to a slope of 1 and halfLife 1.0" should " over a year" in {

    val halfLife: Double = 1.0
    val decayedValues = toDecayedValues(slopes.zip(hundredDays), halfLife, None)
    assert(decayedValues(0).value === 0.0)
    assert(decayedValues(90).value === (90.0 +- 0.02))
    assert(decayedValues(180).value === (180.0 +- 1.75))
    assert(decayedValues(270).value === (270.0 +- 0.02))
    assert(decayedValues(360).value === (360.0 +- 1.75))

    assert(decayedValues(0).scaledTime === 0.0)
    assert(decayedValues(90).scaledTime === (90.0 +- 0.02))
    assert(decayedValues(180).scaledTime === (180.0 +- 1.75))
    assert(decayedValues(270).scaledTime === (270.0 +- 0.02))
    assert(decayedValues(360).scaledTime === (360.0 +- 1.75))
  }

  "A DecayedValue applied to a slope of 1 and halfLife 10.0" should " over a year" in {

    val halfLife: Double = 10.0
    val decayedValues = toDecayedValues(slopes.zip(hundredDays), halfLife, None)
    assert(decayedValues(0).value === 0.0)
    assert(decayedValues(90).value === (90.0 +- 0.02))
    assert(decayedValues(180).value === (180.0 +- 1.75))
    assert(decayedValues(270).value === (270.0 +- 0.02))
    assert(decayedValues(360).value === (360.0 +- 1.75))

    assert(decayedValues(0).scaledTime === 0.0)
    assert(decayedValues(90).scaledTime === (90.0 +- 0.02))
    assert(decayedValues(180).scaledTime === (180.0 +- 1.75))
    assert(decayedValues(270).scaledTime === (270.0 +- 0.02))
    assert(decayedValues(360).scaledTime === (360.0 +- 1.75))
  }

  "A DecayedValue applied to a slope of 1 and halfLife 100.0" should " over a year" in {

    val halfLife: Double = 100.0
    val decayedValues = toDecayedValues(slopes.zip(hundredDays), halfLife, None)
    assert(decayedValues(0).value === 0.0)
    assert(decayedValues(90).value === (90.0 +- 0.02))
    assert(decayedValues(180).value === (180.0 +- 1.75))
    assert(decayedValues(270).value === (270.0 +- 0.02))
    assert(decayedValues(360).value === (360.0 +- 1.75))

    assert(decayedValues(0).scaledTime === 0.0)
    assert(decayedValues(90).scaledTime === (90.0 +- 0.02))
    assert(decayedValues(180).scaledTime === (180.0 +- 1.75))
    assert(decayedValues(270).scaledTime === (270.0 +- 0.02))
    assert(decayedValues(360).scaledTime === (360.0 +- 1.75))
  }

  "A DecayedValue applied to a slope of 1 and halfLife 200.0" should " over a year" in {

    val halfLife: Double = 200.0
    val decayedValues = toDecayedValues(slopes.zip(hundredDays), halfLife, None)
    assert(decayedValues(0).value === 0.0)
    assert(decayedValues(90).value === (90.0 +- 0.02))
    assert(decayedValues(180).value === (180.0 +- 1.75))
    assert(decayedValues(270).value === (270.0 +- 0.02))
    assert(decayedValues(360).value === (360.0 +- 1.75))

    assert(decayedValues(0).scaledTime === 0.0)
    assert(decayedValues(90).scaledTime === (90.0 +- 0.02))
    assert(decayedValues(180).scaledTime === (180.0 +- 1.75))
    assert(decayedValues(270).scaledTime === (270.0 +- 0.02))
    assert(decayedValues(360).scaledTime === (360.0 +- 1.75))
  }
}