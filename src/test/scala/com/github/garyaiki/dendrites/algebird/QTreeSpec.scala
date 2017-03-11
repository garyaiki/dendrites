/**

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
/** @see http://en.wikipedia.org/wiki/Interquartile_mean
  * @see http://en.wikipedia.org/wiki/Interquartile_range
  */
package com.github.garyaiki.dendrites.algebird

import com.twitter.algebird.QTree
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import com.github.garyaiki.dendrites.algebird.fixtures.QTreeBuilder

/**
  *
  * @see [[http://en.wikipedia.org/wiki/Interquartile_mean Interquartile_mean]]
  * @see [[http://en.wikipedia.org/wiki/Interquartile_range Interquartile_range]]
  *
  * @author Gary Struthers
  *
  */
class QTreeSpec extends FlatSpecLike with QTreeBuilder {

  "A QTree" should "be created from a Sequence of BigDecimal" in {
    val size = bigDecimals.size
    qtBD.count shouldBe size
    qtBD.level shouldBe level
    assert(qtBD.range >= size)
    val lb = qtBD.lowerBound
    assert(lb <= bigDecimals.min)
    val ub = qtBD.upperBound
    assert(ub >= bigDecimals.max)
    val fst = qtBD.quantileBounds(0.25)
    fst._1 should be >= q1
    fst._2 should be <= q1 + 0.0001
    val snd = qtBD.quantileBounds(0.5)
    snd._1 should be >= q2
    snd._2 should be <= q2 + 0.0001
    val trd = qtBD.quantileBounds(0.75)
    trd._1 should be >= 115.0
    trd._2 should be <= 115.0 + 1.0001
    val sum = bigDecimals.sum
    qtBD.rangeSumBounds(lb, ub) shouldBe (sum, sum)
    qtBD.rangeCountBounds(lb, ub) shouldBe (size, size)
  }

  it should "return InterQuartileMean from BigDecimals" in {
    val iqm = qtBD.interQuartileMean
    assert(iqm._1 > 100.0)
    assert(iqm._2 < 110.1)
  }

  it should "return InterQuartileMean by summing a Sequence of QTree[BigDecimal]" in {
    val qTrees = Vector(qtBD, qtBD2)
    val sumQTree = sumQTrees(qTrees)
    val iqm = sumQTree.interQuartileMean
    iqm._1 should be > 88.5
    iqm._2 should be < 94.8
  }

  it should "be created from a Sequence of BigInt" in {
    val size = bigInts.size
    qtBI.count shouldBe size
    qtBI.level shouldBe level
    assert(qtBI.range >= size)
    val lb = qtBI.lowerBound
    assert(lb <= bigInts.min.toDouble)
    val ub = qtBI.upperBound
    assert(ub >= bigInts.max.toDouble)
    val fst = qtBI.quantileBounds(0.25)
    fst._1 should be >= q1
    fst._2 should be <= q1 + 0.0001
    val snd = qtBI.quantileBounds(0.5)
    snd._1 should be >= q2
    snd._2 should be <= q2 + 0.0001
    val trd = qtBI.quantileBounds(0.75)
    trd._1 should be >= 115.0
    trd._2 should be <= 115.0 + 1.0001
    val sum = bigInts.sum
    qtBI.rangeSumBounds(lb, ub) shouldBe (sum, sum)
    qtBI.rangeCountBounds(lb, ub) shouldBe (size, size)
  }

  it should "return InterQuartileMean from BigInt" in {
    val iqm = qtBI.interQuartileMean
    iqm._1 should be > 100.0
    iqm._2 should be < 110.1
  }

  it should "return InterQuartileMean by summing a Sequence of QTree[BigInt]" in {
    val qTrees = Vector(qtBI, qtBI2)
    val sumQTree = sumQTrees(qTrees)
    val iqm = sumQTree.interQuartileMean
    iqm._1 should be > 88.5
    iqm._2 < 94.8
  }

  it should "be created from a Sequence of Double" in {
    val size = doubles.size
    qtD.count shouldBe size
    qtD.level shouldBe level + 3 // Why?
    assert(qtD.range >= size)
    val lb = qtD.lowerBound
    assert(lb <= doubles.min)
    val ub = qtD.upperBound
    assert(ub >= doubles.max)
    val fst = qtD.quantileBounds(0.25)
    fst._1 should be >= 110.0 // Why?
    fst._2 should be <= 110 + 0.0001
    val snd = qtD.quantileBounds(0.5)
    snd._1 should be >= 120.0
    snd._2 should be <= 120 + 0.0001
    val trd = qtD.quantileBounds(0.75)
    trd._1 should be >= 130.0
    trd._2 should be <= 130.0 + 0.0001
    val sum = doubles.sum
    qtD.rangeSumBounds(lb, ub) shouldBe (sum, sum)
    qtD.rangeCountBounds(lb, ub) shouldBe (size, size)
  }

  it should "return InterQuartileMean from Doubles" in {
    val iqm = qtD.interQuartileMean
    iqm._1 should be > 75.0
    iqm._2 should be < 125.1
  }

  it should "return InterQuartileMean by summing a Sequence of QTree[Double]" in {
    val qTrees = Vector(qtD, qtD2)
    val sumQTree = sumQTrees(qTrees)
    val iqm = sumQTree.interQuartileMean
    iqm._1 should be > 72.5
    iqm._2 should be < 109.4
  }

  it should "be created from a Sequence of Float" in {
    val size = floats.size
    qtF.count shouldBe size
    qtF.level shouldBe level + 3 // Why?
    assert(qtF.range >= size)
    val lb = qtF.lowerBound
    assert(lb <= floats.min.toDouble)
    val ub = qtF.upperBound
    assert(ub >= floats.max.toDouble)
    val fst = qtF.quantileBounds(0.25)
    fst._1 should be >= 112.0
    fst._2 should be <= 113 + 0.5001
    val snd = qtF.quantileBounds(0.5)
    snd._1 should be >= 119.0
    snd._2 should be <= 119 + 2.1001
    val trd = qtF.quantileBounds(0.75)
    trd._1 should be >= 126.0
    trd._2 should be <= 126.0 + 1.7001
    val sum = floats.sum
    qtF.rangeSumBounds(lb, ub) shouldBe (sum, sum)
    qtF.rangeCountBounds(lb, ub) shouldBe (size, size)
  }

  it should "return InterQuartileMean from Floats" in {
    val iqm = qtF.interQuartileMean
    iqm._1 should be > 110.0
    iqm._2 should be < 121.001
  }

  it should "return InterQuartileMean by summing a Sequence of QTree[Float]" in {
    val qTrees = Vector(qtF, qtF2)
    val sumQTree = sumQTrees(qTrees)
    val iqm = sumQTree.interQuartileMean
    iqm._1 should be > 98.5
    iqm._2 should be < 105.7
  }

  it should "be created from a Sequence of Int" in {
    val size = ints.size
    qtI.count shouldBe size
    qtI.level shouldBe level
    assert(qtI.range >= size)
    val lb = qtI.lowerBound
    assert(lb <= ints.min.toDouble)
    val ub = qtI.upperBound
    assert(ub >= ints.max.toDouble)
    val fst = qtI.quantileBounds(0.25)
    fst._1 should be >= q1
    fst._2 should be <= q1 + 0.0001
    val snd = qtI.quantileBounds(0.5)
    snd._1 should be >= q2
    snd._2 should be <= q2 + 0.0001
    val trd = qtI.quantileBounds(0.75)
    trd._1 should be >= 115.0
    trd._2 should be <= 115.0 + 1.001
    val sum = ints.sum
    qtI.rangeSumBounds(lb, ub) shouldBe (sum, sum)
    qtI.rangeCountBounds(lb, ub) shouldBe (size, size)
  }

  it should "return InterQuartileMean from Ints" in {
    val iqm = qtI.interQuartileMean
    assert(iqm._1 > 100.0)
    assert(iqm._2 < 110.1)
  }

  it should "return InterQuartileMean by summing a Sequence of QTree[Int]" in {
    val qTrees = Vector(qtI, qtI2)
    val sumQTree = sumQTrees(qTrees)
    val iqm = sumQTree.interQuartileMean
    assert(iqm._1 > 88.5)
    assert(iqm._2 < 94.8)
  }

  it should "be created from a Sequence of Long" in {
    val size = longs.size
    qtL.count shouldBe size
    qtL.level shouldBe level
    assert(qtL.range >= size)
    val lb = qtL.lowerBound
    assert(lb <= longs.min.toDouble)
    val ub = qtL.upperBound
    assert(ub >= longs.max.toDouble)
    val fst = qtL.quantileBounds(0.25)
    fst._1 should be >= 102.0
    fst._2 should be <= 104.1
    val snd = qtL.quantileBounds(0.5)
    snd._1 should be >= q2
    snd._2 should be <= q2 + 1.001
    val trd = qtL.quantileBounds(0.75)
    trd._1 should be >= 115.0
    trd._2 should be <= 117.0
    val sum = longs.sum
    qtL.rangeSumBounds(lb, ub) shouldBe (sum, sum)
    qtL.rangeCountBounds(lb, ub) shouldBe (size, size)
  }

  it should "return InterQuartileMean from Longs" in {
    val iqm = qtL.interQuartileMean
    assert(iqm._1 > 100.0)
    assert(iqm._2 < 119.13)
  }

  it should "return InterQuartileMean by summing a Sequence of QTree[Long]" in {
    val qTrees = Vector(qtL, qtL2)
    val sumQTree = sumQTrees(qTrees)
    val iqm = sumQTree.interQuartileMean
    assert(iqm._1 > 88.6)
    assert(iqm._2 < 99.1)
  }

  "A Sequence of QTrees" should "be created from a Sequence of BigDecimal" in {
    val qTrees: Seq[QTree[BigDecimal]] = buildQTrees(bigDecimals)
    qTrees.size shouldBe bigDecimals.size
  }
}
