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
    val sum = sumOption(bigDecimals).get
    assert(sum === bigDecimal)
  }

  "A Sequence of BigInt" should "be summed by a Monoid" in {
    assert(sum(bigInts) === bigInt)
  }

  "A Sequence of Boolean" should "be summed by a Monoid" in {
    assert(sum(booleans) === true)
  }
  
  "A Sequence of Double" should "be summed by a Monoid" in {
    assert(sum(doubles) === double)
  }
  
  "A Sequence of Float" should "be summed by a Monoid" in {
    assert(sum(floats) === float)
  }

  "A Sequence of Int" should "be summed by a Monoid" in {
    assert(sum(ints) === int)
  }

  "A Sequence of Long" should "be summed by a Monoid" in {
    assert(sum(longs) === long)
  }

  "A Sequence of String" should "be summed by a Monoid" in {
    assert(sum(strings) === string)
  }

  "A Sequence of Option[BigDecimal]" should "be summed by a Monoid" in {
    assert(sum(optBigDecs.flatten) === bigDecimal)
  }

  "A Sequence of Option[BigInt]" should "be summed by a Monoid" in {
    assert(sum(optBigInts.flatten) === bigInt)
  }

  "A Sequence of Option[Boolean]" should "be summed by a Monoid" in {
    assert(sum(optBooleans.flatten) === true)
  }

  "A Sequence of Option[Double]" should "be summed by a Monoid" in {
    assert(sum(optDoubles.flatten) === double)
  }

  "A Sequence of Option[Float]" should "be summed by a Monoid" in {
    assert(sum(optFloats.flatten) === float)
  }

  "A Sequence of Option[Int]" should "be summed by a Monoid" in {
    assert(sum(optInts.flatten) === int)
  }

  "A Sequence of Option[Long]" should "be summed by a Monoid" in {
    assert(sum(optLongs.flatten) === long)
  }

  "A Sequence of Option[String]" should "be summed by a Monoid" in {
    assert(sum(optStrs.flatten) === string)
  }

  "A Sequence of Either[String, BigDecimal]" should "be summed by a Monoid" in {
    assert(sum(eithBigDecs) === eithBigDec)
  }

  "A Sequence of Either[String, BigInt]" should "be summed by a Monoid" in {
    assert(sum(eithBigInts) === eithBigInt)
  }

  "A Sequence of Either[String, Boolean]" should "be summed by a Monoid" in {
    val s = sum(eithBooleans)
    assert(s.right.get === true)
  }

  "A Sequence of Either[String, Double]" should "be summed by a Monoid" in {
    assert(sum(eithDoubles) === eithDouble)
  }

  "A Sequence of Either[String, Float]" should "be summed by a Monoid" in {
    assert(sum(eithFloats) === eithFloat)
  }

  "A Sequence of Either[String, Int]" should "be summed by a Monoid" in {
    assert(sum(eithInts) === eithInt)
  }

  "A Sequence of Either[String, Long]" should "be summed by a Monoid" in {
    assert(sum(eithLongs) === eithLong)
  }

  "A Sequence of Either[String, String]" should "be summed by a Monoid" in {
    assert(sum(eithStrs) === eithStr)
  }
}