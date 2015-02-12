/**
  */
package org.gs.algebird.fixtures

import org.gs._
import org.gs.fixtures.TestValuesBuilder
import org.gs.algebird._
//import language.postfixOps
//import util.Random
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
  implicit val qtBISemigroup = new QTreeSemigroup[BigInt](level)
  val qtBI = buildQTree[BigInt](bigInts)
  implicit val qtSemigroup = new QTreeSemigroup[Double](level)
  val qtD = buildQTree[Double](doubles)
  implicit val qtFSemigroup = new QTreeSemigroup[Float](level)
  val qtF = buildQTree[Float](floats)
  implicit val qtISemigroup = new QTreeSemigroup[Int](level)
  val qtI = buildQTree[Int](ints)
  implicit val qtLSemigroup = new QTreeSemigroup[Long](level)
  val qtL = buildQTree[Long](longs)
}
