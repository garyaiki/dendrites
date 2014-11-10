/** aggregate comparable objects, numerics, and strings
  *
  * @see https://github.com/twitter/algebird...Ring.scala
  */
package org.gs.akka.algebird

import math.Ordered

import com.twitter.algebird._
import com.twitter.algebird.Operators._

/** @author garystruthers
  *
  */
object RIng {

  val r = Ring
  val one = r.one[Int]
  val times = r times (1, 2)
  val product = r.product(List(1, 2, 3, 4))
  val atm = r.asTimesMonoid
  val productOpt = r.productOption(List(1, 2, 3, 4))

  val ir = IntRing
  val oneI = ir.one
  val zeroI = ir.zero
  val negateI = ir.negate(2)
  val plusI = ir plus (1, 2)
  val minusI = ir minus (1, 2)
  val timesI = ir times (1, 2)

}