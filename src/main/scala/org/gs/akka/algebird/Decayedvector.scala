/** Compute moving average over a fixed size data set where the weight of an item decreased with age
  *
  * @see https://github.com/twitter/algebird/wiki/Using-DecayedValue-as-moving-average
  * @see https://github.com/twitter/algebird/...DecayedValue.scala
  * @see http://donlehmanjr.com/Science/03%20Decay%20Ave/032.htm
  */
package org.gs.akka.algebird

import com.twitter.algebird._

/** @author garystruthers
  *
  */
object Decayedvector {

  val data = {
    val rnd = new scala.util.Random
    (1 to 10).map { _ => rnd.nextInt(1000).toDouble }.toVector
  }

  val HalfLife = 10.0
  
  val dv = DecayedVector

  val buildWithHalflife = DecayedVector buildWithHalflife (data, 0.0, HalfLife)
  implicit val monoid = DecayedVector.monoidWithEpsilon(1e-3)
  val scaledTime = buildWithHalflife.scaledTime
  val buildWithHalflife2 = DecayedVector buildWithHalflife (data, buildWithHalflife.scaledTime, HalfLife)
  //val sumDV = DecayedVector.scaledPlus(buildWithHalflife, buildWithHalflife2, 1e-3)

}