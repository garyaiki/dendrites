/**
  */
package org.gs.algebird

import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import org.gs.fixtures.{ CaseClassLike, TestValuesBuilder }
import org.gs.algebird._
import org.gs._
import com.twitter.algebird._

/** @author garystruthers
  *
  */
class AveragedSpec extends FlatSpecLike with TestValuesBuilder {

  "An AveragedValue of BigDecimals" should "be near their mean" in {
    val av = avg(bigDecimals)
    assert(av.count === bigDecimals.size)
    val m = mean(bigDecimals)
    assert(av.value === (m.right.get.toDouble +- 0.005))
  }

  it should "be associative" in {
    val avgL = avg(bigDecimals)
    val avgR = avg(bigDecimals2)
    val m = mean(bigDecimals ++ bigDecimals2)
    val av = AveragedGroup.plus(avgL, avgR)
    assert(av.value === (m.right.get.toDouble +- 0.005))
  }

  "An AveragedValue of BigInts" should "be near their mean" in {
    val av = avg(bigInts)
    val m = mean(bigInts)
    assert(av.count === bigInts.size)
    assert(av.value === (m.right.get.toDouble +- 0.5))
  }

  it should "be associative" in {
    val avgL = avg(bigInts)
    val avgR = avg(bigInts2)
    val m = mean(bigInts ++ bigInts2)
    val av = AveragedGroup.plus(avgL, avgR)
    assert(av.value === (m.right.get.toDouble +- 0.5))
  }

  "An AveragedValue of Doubles" should "be near their mean" in {
    val av = avg(doubles)
    val m = mean(doubles)
    assert(av.count === doubles.size)
    assert(av.value === (m.right.get +- 0.005))
  }

  it should "be associative" in {
    val avgL = avg(doubles)
    val avgR = avg(doubles2)
    val m = mean(doubles ++ doubles2)
    val av = AveragedGroup.plus(avgL, avgR)
    assert(av.value === (m.right.get.toDouble +- 0.005))
  }

  "An AveragedValue of Floats" should "be near their mean" in {
    val av = avg(floats)
    val m = mean(floats)
    assert(av.count === floats.size)
    assert(av.value === (m.right.get.toDouble +- 0.005))
  }

  it should "be associative" in {
    val avgL = avg(floats)
    val avgR = avg(floats2)
    val m = mean(floats ++ floats2)
    val av = AveragedGroup.plus(avgL, avgR)
    assert(av.value === (m.right.get.toDouble +- 0.005))
  }
  
  "An AveragedValue of Ints" should "be near their mean" in {
    val av = avg(ints)
    val m = mean(ints)
    assert(av.count === ints.size)
    assert(av.value === (m.right.get.toDouble +- 0.5))
  }

  it should "be associative" in {
    val avgL = avg(ints)
    val avgR = avg(ints2)
    val m = mean(ints ++ ints2)
    val av = AveragedGroup.plus(avgL, avgR)
    assert(av.value === (m.right.get.toDouble +- 0.5))
  }
  
  "An AveragedValue of Longs" should "be near their mean" in {
    val av = avg(longs)
    val m = mean(ints)
    assert(av.count === ints.size)
    assert(av.value === (m.right.get.toDouble +- 0.5))
  }

  it should "be associative" in {
    val avgL = avg(longs)
    val avgR = avg(longs2)
    val m = mean(longs ++ longs2)
    val av = AveragedGroup.plus(avgL, avgR)
    assert(av.value === (m.right.get.toDouble +- 0.5))
  }

}
