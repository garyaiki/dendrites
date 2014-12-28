/**
  */
package org.gs.algebird

import org.scalatest.{ FlatSpecLike, Matchers }
import org.gs._
import org.gs.algebird._
import org.gs.fixtures.{CaseClassLike, TestValuesBuilder}
import com.twitter.algebird.HyperLogLogAggregator
/** @author garystruthers
  *
  */
class HyperLogLogSpec extends FlatSpecLike with TestValuesBuilder {
  implicit val ag = HyperLogLogAggregator(12)

  "A Sequence of Int" should "be summed by a HyperLogLog" in {
    val approx = estDistinctVals(ints)
    assert(approx.estimate === ints.size)
  }

  "A Sequence of Option[Int]" should "be summed by a HyperLogLog" in {
    val approx = estDistinctVals(optInts.flatten)
    assert(approx.estimate === optInts.flatten.size)
  }

  "A Sequence of Either[String, Int]" should "be summed by a HyperLogLog" in {
    val approx = estDistinctVals(filterRight(eithInts))
    assert(approx.estimate === filterRight(eithInts).size)
  }
}