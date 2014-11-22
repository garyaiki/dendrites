/** aggregate comparable objects, numerics, and strings
  *
  * @see https://github.com/twitter/algebird...Group.scala
  */
package org.gs.akka.algebird

import math.Ordered

import com.twitter.algebird._
import com.twitter.algebird.Operators._

/** @author garystruthers
  *
  */
object GRoup {

  val g = Group.minus[Int](1, 2)
  val gE = Group.equiv[Int]
  val f = gE.equiv(1, 2)
  val t = gE.equiv(2, 2)

  val bi = BigInt(10)
  val gIt = Group.intTimes[Int](bi, 2)
  val gItd = Group.intTimes[Double](bi, 3.3)
  val oG = new OptionGroup[Int]
  val nz0 = oG.isNonZero(Some(0))
  val nz1 = oG.isNonZero(Some(1))
  val nzN = oG.isNonZero(None) //false
  val nn0 = oG.negate(Some(0)) // Some(0)
  val nn1 = oG.negate(Some(1)) // Some(-1)
  val nnN = oG.negate(None) //None

  def conditionalNegate[A](x: A, f: A => Boolean)(implicit ev: Group[A]): A =
      if (f(x)) ev.negate(x) else x
}