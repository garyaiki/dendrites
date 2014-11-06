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

  val HalfLife = 10.0
  val normalization = HalfLife / math.log(2)

  implicit val monoid = DecayedValue.monoidWithEpsilon(1e-3)

  data.zipWithIndex.scanLeft(Monoid.zero[DecayedValue]) { (previous, data) =>
    val (value, time) = data
    Monoid.plus(previous, DecayedValue.build(value, time, HalfLife))
  }
  Platform.currentTime
  val previous = DecayedValue.build(350.0, Platform.currentTime.toDouble, HalfLife)
  val next = DecayedValue.build(351.0, Platform.currentTime.toDouble, HalfLife)
  val decayed = Monoid.plus(previous, next)

}