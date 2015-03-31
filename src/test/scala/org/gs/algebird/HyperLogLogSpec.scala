/**
  */
package org.gs.algebird

import org.scalatest.{ FlatSpecLike, Matchers }
import org.gs._
import org.gs.algebird._
import org.gs.fixtures.{ CaseClassLike, TestValuesBuilder }
import com.twitter.algebird.HyperLogLogAggregator
/** @author garystruthers
  *
  */
class HyperLogLogSpec extends FlatSpecLike with TestValuesBuilder {
  implicit val ag = HyperLogLogAggregator(12)

  "A HyperLogLog" should "estimate total number of integers from a Sequence of HLL" in {
    val hll = createHLL(ints)
    val hll2 = createHLL(ints2)
    val hlls = Vector(hll, hll2)
    val sum = hlls.reduce(_ + _)
    assert(sum.estimatedSize === ints.size + ints2.size)
  }

  it should "estimate total number of longs from a Sequence of HLL" in {
    val hll = createHLL(longs)
    val hll2 = createHLL(longs2)
    val hlls = Vector(hll, hll2)
    val sum = hlls.reduce(_ + _)
    assert(sum.estimatedSize === longs.size + longs2.size)
  }
  
  it should "estimate total number of integers from a Sequence of Approximate" in {
    val approx = createHLL(ints).approximateSize
    val approx2 = createHLL(ints2).approximateSize
    val approxs = Vector(approx, approx2)
    val sum = approxs.reduce(_ + _)
    assert(sum.estimate === ints.size + ints2.size)
  }

  it should "estimate total number of longs from a Sequence of Approximate" in {
    val approx = createHLL(longs).approximateSize
    val approx2 = createHLL(longs2).approximateSize
    val approxs = Vector(approx, approx2)
    val sum = approxs.reduce(_ + _)
    assert(sum.estimate === longs.size + longs2.size)
  }
}
