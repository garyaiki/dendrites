/** aggregate comparable objects, numerics, and strings
  *
  * @see https://github.com/twitter/algebird...OrderedSemigroup.scala
  */
package org.gs.akka.algebird

import math.Ordered

import com.twitter.algebird._
import com.twitter.algebird.Operators._

/** @author garystruthers
  *
  */
object SemiGroup {

  val sg = Semigroup.plus[Int](1, 2)
  val eSg = new EitherSemigroup[String, Int]
  val ePlusRR = eSg.plus(Right(1), Right(2))
  val ePlusLR = eSg.plus(Left("1"), Right(2))
  val ePlusRL = eSg.plus(Right(1), Left("2"))
  val ePlusLL = eSg.plus(Left("1"), Left("2"))
  val bi = BigInt(10)
  val sgIt = Semigroup.intTimes[Int](bi, 2)
  val sgItd = Semigroup.intTimes[Double](bi, 3.3)
  val sgSo = Semigroup.sumOption[Int](List(1, 2, 3, 4))
  val sgSos = Semigroup.sumOption[String](List("1", "2", "3", "4"))


  def sumOption[A](xs: List[A])(implicit ev: Semigroup[A]): Option[A] = ev.sumOption(xs)

}