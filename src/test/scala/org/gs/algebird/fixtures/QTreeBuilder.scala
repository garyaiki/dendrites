/**
  */
package org.gs.algebird.fixtures

import com.twitter.algebird._
import org.gs._
import org.gs.fixtures.TestValuesBuilder
import org.gs.algebird._
import org.scalatest._


/** @author garystruthers
  *
  */
trait QTreeBuilder extends SuiteMixin with TestValuesBuilder { this: Suite =>

  abstract override def withFixture(test: NoArgTest): Outcome = {
    super.withFixture(test)
  }

  val level = 5
  val q1 = 103.0
  val q2 = 110.0
  val q3 = 115.0
  implicit val qtBDSemigroup = new QTreeSemigroup[BigDecimal](level)
  val qtBD = buildQTree(bigDecimals)
  val qtBD2 = buildQTree(bigDecimals2)
  implicit val qtBISemigroup = new QTreeSemigroup[BigInt](level)
  val qtBI = buildQTree(bigInts)
  val qtBI2 = buildQTree(bigInts2)
  implicit val qtSemigroup = new QTreeSemigroup[Double](level)
  val qtD = buildQTree(doubles)
  val qtD2 = buildQTree(doubles2)
  implicit val qtFSemigroup = new QTreeSemigroup[Float](level)
  val qtF = buildQTree(floats)
  val qtF2 = buildQTree(floats2)
  implicit val qtISemigroup = new QTreeSemigroup[Int](level)
  val qtI = buildQTree(ints)
  val qtI2 = buildQTree(ints2)
  implicit val qtLSemigroup = new QTreeSemigroup[Long](level)
  val qtL = buildQTree(longs)
  val qtL2 = buildQTree(longs2)
}
