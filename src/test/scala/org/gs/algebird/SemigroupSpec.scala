/**
  */
package org.gs.algebird

import org.scalatest.{ FlatSpecLike, Matchers }
import org.gs.TestValuesBuilder
import org.gs.algebird._

/** @author garystruthers
  *
  */
class SemigroupSpec extends FlatSpecLike with TestValuesBuilder {

  "A Sequence of BigDecimal" should "be summed by a Semigroup" in {
    val sum = sumOption(bigDecimals)
    assert(sum.get === 2847)
  }

  "A Sequence of BigInt" should "be summed by a Semigroup" in {
    val sum = sumOption(bigInts)
    assert(sum.get === 2847)
  }

  "A Sequence of Boolean" should "be summed by a Semigroup" in {
    val sum = sumOption(booleans)
    assert(sum.get === true)
  }

  "A Sequence of Double" should "be summed by a Semigroup" in {
    val sum = sumOption(doubles)
    assert(sum.get === 3130.0)
  }

  "A Sequence of Float" should "be summed by a Semigroup" in {
    val sum = sumOption(floats)
    assert(sum.get === 3131.7.toFloat)
  }

  "A Sequence of Int" should "be summed by a Semigroup" in {
    val sum = sumOption(ints)
    assert(sum.get === 2847)
  }

  "A Sequence of Long" should "be summed by a Semigroup" in {
    val sum = sumOption(longs)
    assert(sum.get === 2847)
  }

  "A Sequence of String" should "be summed by a Semigroup" in {
    val sum = sumOption(strings)
    assert(sum.get === "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
  }

  "A Sequence of Option[BigDecimal]" should "be summed by a Semigroup" in {
    val sum = sumOption(optBigDecs.flatten)
    assert(sum.get === 2847)
  }

  "A Sequence of Option[BigInt]" should "be summed by a Semigroup" in {
    val sum = sumOption(optBigInts.flatten)
    assert(sum.get === 2847)
  }

  "A Sequence of Option[Boolean]" should "be summed by a Semigroup" in {
    val sum = sumOption(optBooleans.flatten)
    assert(sum.get === true)
  }

  "A Sequence of Option[Double]" should "be summed by a Semigroup" in {
    val sum = sumOption(optDoubles.flatten)
    assert(sum.get === 3130.0)
  }

  "A Sequence of Option[Float]" should "be summed by a Semigroup" in {
    val sum = sumOption(optFloats.flatten)
    assert(sum.get === 3131.7.toFloat)
  }

  "A Sequence of Option[Int]" should "be summed by a Semigroup" in {
    val sum = sumOption(optInts.flatten)
    assert(sum.get === 2847)
  }

  "A Sequence of Option[Long]" should "be summed by a Semigroup" in {
    val sum = sumOption(optLongs.flatten)
    assert(sum.get === 2847)
  }

  "A Sequence of Option[String]" should "be summed by a Semigroup" in {
    val sum = sumOption(optStrs.flatten)
    assert(sum.get === "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
  }

  "A Sequence of Either[String, BigDecimal]" should "be summed by a Semigroup" in {
    val sum = sumOption(eithBigDecs).get
    assert(sum.right.get === 2847)
  }

  "A Sequence of Either[String, BigInt]" should "be summed by a Semigroup" in {
    val sum = sumOption(eithBigInts)
    assert(sum.get === 2847)
  }

  "A Sequence of Either[String, Boolean]" should "be summed by a Semigroup" in {
    val sum = sumOption(eithBooleans)
    assert(sum.get === true)
  }

  "A Sequence of Either[String, Double]" should "be summed by a Semigroup" in {
    val sum = sumOption(eithDoubles)
    assert(sum.get === 3130.0)
  }

  "A Sequence of Either[String, Float]" should "be summed by a Semigroup" in {
    val sum = sumOption(eithFloats)
    assert(sum.get === 3131.7.toFloat)
  }

  "A Sequence of Either[String, Int]" should "be summed by a Semigroup" in {
    val sum = sumOption(eithInts)
    assert(sum.get === 2847)
  }

  "A Sequence of Either[String, Long]" should "be summed by a Semigroup" in {
    val sum = sumOption(eithLongs)
    assert(sum.get === 2847)
  }

  "A Sequence of Either[String, String]" should "be summed by a Semigroup" in {
    val sum = sumOption(eithStrs).get
    assert(sum.right.get === "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
  }
}