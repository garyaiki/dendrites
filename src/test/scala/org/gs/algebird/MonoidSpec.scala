/**
  */
package org.gs.algebird

import org.scalatest.{ FlatSpecLike, Matchers }
import org.gs.TestValuesBuilder
import org.gs.algebird._

/** @author garystruthers
  *
  */
class MonoidSpec extends FlatSpecLike with TestValuesBuilder {

  "A Sequence of BigDecimal" should "be summed by a Monoid" in {
    val sum = sumOption(bigDecimals)
    assert(sum.get === 2847)
  }

  "A Sequence of BigInt" should "be summed by a Monoid" in {
    assert(sum(bigInts) === 2847)
  }

  "A Sequence of Boolean" should "be summed by a Monoid" in {
    assert(sum(booleans) === true)
  }
  
  "A Sequence of Double" should "be summed by a Monoid" in {
    assert(sum(doubles) === 3130.0)
  }
  
  "A Sequence of Float" should "be summed by a Monoid" in {
    assert(sum(floats) === 3131.7.toFloat)
  }

  "A Sequence of Int" should "be summed by a Monoid" in {
    assert(sum(ints) === 2847)
  }

  "A Sequence of Long" should "be summed by a Monoid" in {
    assert(sum(longs) === 2847)
  }

  "A Sequence of String" should "be summed by a Monoid" in {
    assert(sum(strings) === "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
  }
}