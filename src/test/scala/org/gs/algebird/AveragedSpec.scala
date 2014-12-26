/**
  */
package org.gs.algebird

import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import org.gs.TestValuesBuilder
import org.gs.algebird._
import org.gs._
import com.twitter.algebird._

/** @author garystruthers
  *
  */
class AveragedSpec extends FlatSpecLike with TestValuesBuilder {

  "A Sequence of BigDecimal" should "return an  AveragedValue" in {
    val sum = avg(bigDecimals)
    assert(sum.count === bigDecimals.size)
    val m = mean(bigDecimals)
    assert(sum.value === m.right.get)
  }

  "A Sequence of BigInt" should "return an  AveragedValue" in {
    val sum = avg(bigInts)
    val m = mean(bigInts)
    assert(sum.count === bigInts.size)
    assert(sum.value === (m.right.get.toDouble +- 0.5))
  }

  "A Sequence of Double" should "return an  AveragedValue" in {
    val sum = avg(doubles)
    val m = mean(doubles)
    assert(sum.count === doubles.size)
    assert(sum.value === (m.right.get +- 0.005))
  }

  "A Sequence of Float" should "return an  AveragedValue" in {
    val sum = avg(floats)
    val m = mean(floats)
    assert(sum.count === floats.size)
    assert(sum.value === (m.right.get.toDouble +- 0.005))
  }

  "A Sequence of Int" should "return an  AveragedValue" in {
    val sum = avg(ints)
    val m = mean(ints)
    assert(sum.count === ints.size)
    assert(sum.value === (m.right.get.toDouble +- 0.5))
  }

  "A Sequence of Long" should "return an  AveragedValue" in {
    val sum = avg(longs)
    val m = mean(ints)
    assert(sum.count === ints.size)
    assert(sum.value === (m.right.get.toDouble +- 0.5))
  }
}