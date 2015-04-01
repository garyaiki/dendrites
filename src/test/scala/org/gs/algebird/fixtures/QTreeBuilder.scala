/**
  */
package org.gs.algebird.fixtures

import org.gs._
import org.gs.fixtures.TestValuesBuilder
import org.gs.algebird._
import org.scalatest._
import com.twitter.algebird._

/** @author garystruthers
  *
  */
trait QTreeBuilder extends SuiteMixin with TestValuesBuilder { this: Suite =>

  abstract override def withFixture(test: NoArgTest): Outcome = {
    super.withFixture(test)
  }

  val level = 5
  val q1 = 102.0
  val q2 = 109.0
  val q3 = 115.0
  implicit val qtBDSemigroup = new QTreeSemigroup[BigDecimal](level)
  val qtBD = buildQTree[BigDecimal](bigDecimals)
  val qtBD2 = buildQTree[BigDecimal](bigDecimals2)
  implicit val qtBISemigroup = new QTreeSemigroup[BigInt](level)
  val qtBI = buildQTree[BigInt](bigInts)
  val qtBI2 = buildQTree[BigInt](bigInts2)
  implicit val qtSemigroup = new QTreeSemigroup[Double](level)
  val qtD = buildQTree[Double](doubles)
  val qtD2 = buildQTree[Double](doubles2)
  implicit val qtFSemigroup = new QTreeSemigroup[Float](level)
  val qtF = buildQTree[Float](floats)
  val qtF2 = buildQTree[Float](floats2)
  implicit val qtISemigroup = new QTreeSemigroup[Int](level)
  val qtI = buildQTree[Int](ints)
  val qtI2 = buildQTree[Int](ints2)
  implicit val qtLSemigroup = new QTreeSemigroup[Long](level)
  val qtL = buildQTree[Long](longs)
  val qtL2 = buildQTree[Long](longs2)
}
