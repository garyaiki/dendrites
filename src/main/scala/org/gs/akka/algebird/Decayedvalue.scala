/** Compute moving average over a fixed size data set where the weight of an item decreased with age
  *
  * @see https://github.com/twitter/algebird/wiki/Using-DecayedValue-as-moving-average
  * @see https://github.com/twitter/algebird/...DecayedValue.scala
  * @see http://donlehmanjr.com/Science/03%20Decay%20Ave/032.htm
  */
package org.gs.akka.algebird

import com.twitter.algebird._
import scala.compat.Platform

/** @author garystruthers
  *
  */
object Decayedvalue {

  val data = {
    val rnd = new scala.util.Random
    (1 to 10).map { _ => rnd.nextInt(1000).toDouble }.toSeq
  }
  val zippedDataTime = data.zipWithIndex

  val HalfLife = 10.0
  val normalization = HalfLife / math.log(2)

  implicit val monoid = DecayedValue.monoidWithEpsilon(1e-3)

  val allZipp = zippedDataTime.scanLeft(Monoid.zero[DecayedValue]) { (previous, zippedDataTime) =>
    Monoid.plus(previous, DecayedValue.build(zippedDataTime._1, zippedDataTime._2, HalfLife))
  }

  val splitDateTime = zippedDataTime splitAt (zippedDataTime.size / 2)
  val firstDateTime = splitDateTime._1
  val lastDateTime = splitDateTime._2

  val firstZipp = firstDateTime.scanLeft(Monoid.zero[DecayedValue]) { (previous, firstDateTime) =>
    Monoid.plus(previous, DecayedValue.build(firstDateTime._1, firstDateTime._2, HalfLife))
  }

  val nextZipp = lastDateTime.scanLeft(firstZipp.last) { (previous, lastDateTime) =>
    Monoid.plus(previous, DecayedValue.build(lastDateTime._1, lastDateTime._2, HalfLife))
  }



}