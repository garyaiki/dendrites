/**
  */
package org.gs.algebird

import org.scalatest.{ FlatSpecLike, Matchers }
import org.gs._
import org.gs.algebird._
import org.gs.fixtures.{CaseClassLike, TestValuesBuilder}

/** @author garystruthers
  *
  */
class SemigroupSpec extends FlatSpecLike with TestValuesBuilder {

  "A Sequence of BigDecimal" should "be summed by a Semigroup" in {
    val sum = sumOption(bigDecimals)
    assert(sum.get === bigDecimals.sum)
  }

  "A Sequence of BigInt" should "be summed by a Semigroup" in {
    val sum = sumOption(bigInts)
    assert(sum.get === bigInts.sum)
  }

  "A Sequence of Boolean" should "be summed by a Semigroup" in {
    val sum = sumOption(booleans)
    assert(sum.get === true)
  }

  "A Sequence of Double" should "be summed by a Semigroup" in {
    val sum = sumOption(doubles)
    assert(sum.get === doubles.sum)
  }

  "A Sequence of Float" should "be summed by a Semigroup" in {
    val sum = sumOption(floats)
    assert(sum.get === floats.sum)
  }

  "A Sequence of Int" should "be summed by a Semigroup" in {
    val sum = sumOption(ints)
    assert(sum.get === ints.sum)
  }

  "A Sequence of Long" should "be summed by a Semigroup" in {
    val sum = sumOption(longs)
    assert(sum.get === longs.sum)
  }

  "A Sequence of String" should "be summed by a Semigroup" in {
    val sum = sumOption(strings)
    assert(sum.get === string)
  }

  "A Sequence of Option[BigDecimal]" should "be summed by a Semigroup" in {
    val sum = sumOption(optBigDecs.flatten)
    assert(sum.get === optBigDecs.flatten.sum)
  }

  "A Sequence of Option[BigInt]" should "be summed by a Semigroup" in {
    val sum = sumOption(optBigInts.flatten)
    assert(sum.get === optBigInts.flatten.sum)
  }

  "A Sequence of Option[Boolean]" should "be summed by a Semigroup" in {
    val sum = sumOption(optBooleans.flatten)
    assert(sum.get === true)
  }

  "A Sequence of Option[Double]" should "be summed by a Semigroup" in {
    val sum = sumOption(optDoubles.flatten)
    assert(sum.get === optDoubles.flatten.sum)
  }

  "A Sequence of Option[Float]" should "be summed by a Semigroup" in {
    val sum = sumOption(optFloats.flatten)
    assert(sum.get === optFloats.flatten.sum)
  }

  "A Sequence of Option[Int]" should "be summed by a Semigroup" in {
    val sum = sumOption(optInts.flatten)
    assert(sum.get === optInts.flatten.sum)
  }

  "A Sequence of Option[Long]" should "be summed by a Semigroup" in {
    val sum = sumOption(optLongs.flatten)
    assert(sum.get === optLongs.flatten.sum)
  }

  "A Sequence of Option[String]" should "be summed by a Semigroup" in {
    val sum = sumOption(optStrs.flatten)
    assert(sum === optStr)
  }

  "A Sequence of Either[String, BigDecimal]" should "be summed by a Semigroup" in {
    val sum = sumOption(eithBigDecs).get
    assert(sum.right.get === filterRight(eithBigDecs).sum)
  }

  "A Sequence of Either[String, BigInt]" should "be summed by a Semigroup" in {
    val sum = sumOption(eithBigInts).get
    assert(sum.right.get === filterRight(eithBigInts).sum)
  }

  "A Sequence of Either[String, Boolean]" should "be summed by a Semigroup" in {
    val sum = sumOption(eithBooleans).get
    assert(sum === Right(true))
  }

  "A Sequence of Either[String, Double]" should "be summed by a Semigroup" in {
    val sum = sumOption(eithDoubles).get
    assert(sum.right.get === filterRight(eithDoubles).sum)
  }

  "A Sequence of Either[String, Float]" should "be summed by a Semigroup" in {
    val sum = sumOption(eithFloats).get
    assert(sum.right.get === filterRight(eithFloats).sum)
  }

  "A Sequence of Either[String, Int]" should "be summed by a Semigroup" in {
    val sum = sumOption(eithInts).get
    assert(sum.right.get === filterRight(eithInts).sum)
  }

  "A Sequence of Either[String, Long]" should "be summed by a Semigroup" in {
    val sum = sumOption(eithLongs).get
    assert(sum.right.get === filterRight(eithLongs).sum)
  }

  "A Sequence of Either[String, String]" should "be summed by a Semigroup" in {
    val sum = sumOption(eithStrs).get
    assert(sum === eithStr)
  }
}