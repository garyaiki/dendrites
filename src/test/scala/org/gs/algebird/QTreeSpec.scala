/** @see http://en.wikipedia.org/wiki/Interquartile_mean
  * @see http://en.wikipedia.org/wiki/Interquartile_range
  */
package org.gs.algebird

import com.twitter.algebird._
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import org.gs._
import org.gs.fixtures.TestValuesBuilder
import org.gs.algebird._
import org.gs.algebird.typeclasses.QTreeLike
import org.gs.algebird.fixtures.QTreeBuilder

/** @author garystruthers
  *
  */
class QTreeSpec extends FlatSpecLike with TestValuesBuilder with QTreeBuilder {

  "A Sequence of BigDecimal" should "return a QTree[BigDecimal]" in {
    val size = bigDecimals.size
    assert(qtBD.count === size)
    assert(qtBD.level === level)
    assert(qtBD.range >= size)
    val lb = qtBD.lowerBound
    assert(lb <= bigDecimals.min)
    val ub = qtBD.upperBound
    assert(ub >= bigDecimals.max)
    val fst = qtBD.quantileBounds(0.25)
    assert(fst._1 >= q1)
    assert(fst._2 <= q1 + 0.0001)
    val snd = qtBD.quantileBounds(0.5)
    assert(snd._1 >= q2)
    assert(snd._2 <= q2 + 0.0001)
    val trd = qtBD.quantileBounds(0.75)
    assert(trd._1 >= 115.0)
    assert(trd._2 <= 115.0 + 0.0001)
    val sum = bigDecimals.sum
    assert(qtBD.rangeSumBounds(lb, ub) === (sum, sum))
    assert(qtBD.rangeCountBounds(lb, ub) === (size, size))
  }
  
  "A Sequence of BigDecimal" should "return an InterQuartileMean" in {
    val iqm = qtBD.interQuartileMean
    assert(iqm._1 > 100.0)
    assert(iqm._2 < 109.1)
  }

  "A Sequence of BigInt" should "return a QTree[BigInt]" in {
    val size = bigInts.size
    assert(qtBI.count === size)
    assert(qtBI.level === level)
    assert(qtBI.range >= size)
    val lb = qtBI.lowerBound
    assert(lb <= bigInts.min.toDouble)
    val ub = qtBI.upperBound
    assert(ub >= bigInts.max.toDouble)
    val fst = qtBI.quantileBounds(0.25)
    assert(fst._1 >= q1)
    assert(fst._2 <= q1 + 0.0001)
    val snd = qtBI.quantileBounds(0.5)
    assert(snd._1 >= q2)
    assert(snd._2 <= q2 + 0.0001)
    val trd = qtBI.quantileBounds(0.75)
    assert(trd._1 >= 115.0)
    assert(trd._2 <= 115.0 + 0.0001)
    val sum = bigInts.sum
    assert(qtBI.rangeSumBounds(lb, ub) === (sum, sum))
    assert(qtBI.rangeCountBounds(lb, ub) === (size, size))
  }
  
  "A Sequence of BigInt" should "return an InterQuartileMean" in {
    val iqm = qtBI.interQuartileMean
    assert(iqm._1 > 100.0)
    assert(iqm._2 < 109.1)
  }
  
  "A Sequence of Double" should "return a QTree[Double]" in {
    val size = doubles.size
    assert(qtD.count === size)
    assert(qtD.level === level + 3)//Why?
    assert(qtD.range >= size)
    val lb = qtD.lowerBound
    assert(lb <= doubles.min)
    val ub = qtD.upperBound
    assert(ub >= doubles.max)
    val fst = qtD.quantileBounds(0.25)
    assert(fst._1 >= 110)//Why?
    assert(fst._2 <= 110 + 0.0001)
    val snd = qtD.quantileBounds(0.5)
    assert(snd._1 >= 120)
    assert(snd._2 <= 120 + 0.0001)
    val trd = qtD.quantileBounds(0.75)
    assert(trd._1 >= 130.0)
    assert(trd._2 <= 130.0 + 0.0001)
    val sum = doubles.sum
    assert(qtD.rangeSumBounds(lb, ub) === (sum, sum))
    assert(qtD.rangeCountBounds(lb, ub) === (size, size))
  }
  
  "A Sequence of Double" should "return an InterQuartileMean" in {
    val iqm = qtD.interQuartileMean
    assert(iqm._1 > 75.0)
    assert(iqm._2 < 125.1)
  }

  "A Sequence of Float" should "return a QTree[Float]" in {
    val size = floats.size
    assert(qtF.count === size)
    assert(qtF.level === level + 3)//Why?
    assert(qtF.range >= size)
    val lb = qtF.lowerBound
    assert(lb <= floats.min.toDouble)
    val ub = qtF.upperBound
    assert(ub >= floats.max.toDouble)
    val fst = qtF.quantileBounds(0.25)
    assert(fst._1 >= 112)
    assert(fst._2 <= 112 + 0.2001)
    val snd = qtF.quantileBounds(0.5)
    assert(snd._1 >= 119)
    assert(snd._2 <= 119 + 0.9001)
    val trd = qtF.quantileBounds(0.75)
    assert(trd._1 >= 126.0)
    assert(trd._2 <= 126.0 + 0.5001)
    val sum = floats.sum
    assert(qtF.rangeSumBounds(lb, ub) === (sum, sum))
    assert(qtF.rangeCountBounds(lb, ub) === (size, size))
  }
  
  "A Sequence of Float" should "return an InterQuartileMean" in {
    val iqm = qtF.interQuartileMean
    assert(iqm._1 > 110.0)
    assert(iqm._2 < 119.901)
  }

  "A Sequence of Int" should "return a QTree[Int]" in {
    val size = ints.size
    assert(qtI.count === size)
    assert(qtI.level === level)
    assert(qtI.range >= size)
    val lb = qtI.lowerBound
    assert(lb <= ints.min.toDouble)
    val ub = qtI.upperBound
    assert(ub >= ints.max.toDouble)
    val fst = qtI.quantileBounds(0.25)
    assert(fst._1 >= q1)
    assert(fst._2 <= q1 + 0.0001)
    val snd = qtI.quantileBounds(0.5)
    assert(snd._1 >= q2)
    assert(snd._2 <= q2 + 0.0001)
    val trd = qtI.quantileBounds(0.75)
    assert(trd._1 >= 115.0)
    assert(trd._2 <= 115.0 + 0.0001)
    val sum = ints.sum
    assert(qtI.rangeSumBounds(lb, ub) === (sum, sum))
    assert(qtI.rangeCountBounds(lb, ub) === (size, size))
  }
  
  "A Sequence of Int" should "return an InterQuartileMean" in {
    val iqm = qtI.interQuartileMean
    assert(iqm._1 > 100.0)
    assert(iqm._2 < 109.1)
  }

  "A Sequence of Long" should "return a QTree[Long]" in {
    val size = longs.size
    assert(qtL.count === size)
    assert(qtL.level === level)
    assert(qtL.range >= size)
    val lb = qtL.lowerBound
    assert(lb <= longs.min.toDouble)
    val ub = qtL.upperBound
    assert(ub >= longs.max.toDouble)
    val fst = qtL.quantileBounds(0.25)
    assert(fst._1 >= 102)
    assert(fst._2 <= 103)
    val snd = qtL.quantileBounds(0.5)
    assert(snd._1 >= q2)
    assert(snd._2 <= q2 + 1)
    val trd = qtL.quantileBounds(0.75)
    assert(trd._1 >= 115.0)
    assert(trd._2 <= 116.0)
    val sum = longs.sum
    assert(qtL.rangeSumBounds(lb, ub) === (sum, sum))
    assert(qtL.rangeCountBounds(lb, ub) === (size, size))
  }
  
  "A Sequence of Long" should "return an InterQuartileMean" in {
    val iqm = qtL.interQuartileMean
    assert(iqm._1 > 100.0)
    assert(iqm._2 < 117.93)
  }
}