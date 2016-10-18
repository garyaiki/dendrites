/**
  */
package org.gs.algebird

import com.twitter.algebird.HyperLogLogAggregator
import org.scalatest.{ FlatSpecLike, Matchers }
import org.scalatest.Matchers._
import org.gs._
import org.gs.algebird._
import org.gs.fixtures.TestValuesBuilder

/** @author garystruthers
  *
  */
class HyperLogLogSpec extends FlatSpecLike with TestValuesBuilder {
  implicit val ag = HyperLogLogAggregator(12)

  "A HyperLogLog" should "estimate number of distinct integers from a Sequence of Int" in {
    val hll = createHLL(ints)
    assert(hll.estimatedSize === (ints.distinct.size.toDouble +- 0.09) )
  }

  it should "map a Sequence of HLL to a Sequence of Approximate" in {
    val hll = createHLL(ints)
    val hll2 = createHLL(ints2)
    val hlls = Vector(hll, hll2)
    val approxs = mapHLL2Approximate(hlls)
    val sum = approxs.reduce(_ + _)
    assert(sum.estimate === ints.distinct.size + ints2.distinct.size)
  }
  
  it should "estimate integers from a Sequence of HLL" in {
    val hll = createHLL(ints)
    val hll2 = createHLL(ints2)
    val hlls = Vector(hll, hll2)
    val sum = hlls.reduce(_ + _)
    assert(sum.estimatedSize === ((ints.distinct.size + ints2.distinct.size).toDouble +- 0.4))
  }

  it should "estimate number of distinct longs from a Sequence of Long" in {
    val hll = createHLL(longs)
    assert(hll.estimatedSize === (longs.distinct.size.toDouble +- 0.09))
  }
  
  it should "estimate number of distinct longs from a Sequence of HLL" in {
    val hll = createHLL(longs)
    val hll2 = createHLL(longs2)
    val hlls = Vector(hll, hll2)
    val sum = hlls.reduce(_ + _)
    assert(sum.estimatedSize === ((longs.distinct.size + longs2.distinct.size).toDouble +- 0.4))
  }

  it should "estimate number of distinct integers from a Sequence of Approximate" in {
    val approx = createHLL(ints).approximateSize
    val approx2 = createHLL(ints2).approximateSize
    val approxs = Vector(approx, approx2)
    val sum = approxs.reduce(_ + _)
    assert(sum.estimate === ints.distinct.size + ints2.distinct.size)
  }

  it should "estimate number of distinct longs from a Sequence of Approximate" in {
    val approx = createHLL(longs).approximateSize
    val approx2 = createHLL(longs2).approximateSize
    val approxs = Vector(approx, approx2)
    val sum = approxs.reduce(_ + _)
    assert(sum.estimate === longs.distinct.size + longs2.distinct.size)
  }

  "A HLL" should "be create-able from BigDecimals" in {
    val hll = createHLL(bigDecimals)
    assert(hll.estimatedSize === (bigDecimals.distinct.size.toDouble +- 0.09) )
  }
  
  it should "be create-able from BigInts" in {
    val hll = createHLL(bigInts)
    assert(hll.estimatedSize === (bigInts.distinct.size.toDouble +- 0.09) )
  }
  
  it should "be create-able from Doubles" in {
    val hlld = createHLL(doubles)
    assert(hlld.estimatedSize === (doubles.distinct.size.toDouble +- 0.09) )
  }
  
  it should "be create-able from Floats" in {
    val hll = createHLL(floats)
    assert(hll.estimatedSize === (floats.distinct.size.toDouble +- 0.09) )
  }
}
