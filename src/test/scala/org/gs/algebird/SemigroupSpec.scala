/**
  */
package org.gs.algebird

import org.scalatest.{ FlatSpecLike, Matchers }
import org.gs.filters._
import org.gs.algebird._
import org.gs.fixtures.{CaseClassLike, TestValuesBuilder}

/** @author garystruthers
  *
  */
class SemigroupSpec extends FlatSpecLike with TestValuesBuilder {

  "A Sequence of BigDecimal" should "be summed by a Semigroup" in {
    val opt = sumOption(bigDecimals)
    assert(opt.get === bigDecimals.sum)
  }

  "A Sequence of BigInt" should "be summed by a Semigroup" in {
    val opt = sumOption(bigInts)
    assert(opt.get === bigInts.sum)
  }

  "A Sequence of Boolean" should "be summed by a Semigroup" in {
    val opt = sumOption(booleans)
    assert(opt.get === true)
  }

  "A Sequence of Double" should "be summed by a Semigroup" in {
    val opt = sumOption(doubles)
    assert(opt.get === doubles.sum)
  }

  "A Sequence of Float" should "be summed by a Semigroup" in {
    val opt = sumOption(floats)
    assert(opt.get === floats.sum)
  }

  "A Sequence of Int" should "be summed by a Semigroup" in {
    val opt = sumOption(ints)
    assert(opt.get === ints.sum)
  }

  "A Sequence of Long" should "be summed by a Semigroup" in {
    val opt = sumOption(longs)
    assert(opt.get === longs.sum)
  }

  "A Sequence of String" should "be summed by a Semigroup" in {
    val opt = sumOption(strings)
    assert(opt.get === string)
  }

  "A Sequence of Option[BigDecimal]" should "be summed by a Semigroup" in {
    val opt = sumOption(optBigDecs.flatten)
    assert(opt.get === optBigDecs.flatten.sum)
  }

  "A Sequence of Option[BigInt]" should "be summed by a Semigroup" in {
    val opt = sumOption(optBigInts.flatten)
    assert(opt.get === optBigInts.flatten.sum)
  }

  "A Sequence of Option[Boolean]" should "be summed by a Semigroup" in {
    val opt = sumOption(optBooleans.flatten)
    assert(opt.get === true)
  }

  "A Sequence of Option[Double]" should "be summed by a Semigroup" in {
    val opt = sumOption(optDoubles.flatten)
    assert(opt.get === optDoubles.flatten.sum)
  }

  "A Sequence of Option[Float]" should "be summed by a Semigroup" in {
    val opt = sumOption(optFloats.flatten)
    assert(opt.get === optFloats.flatten.sum)
  }

  "A Sequence of Option[Int]" should "be summed by a Semigroup" in {
    val opt = sumOption(optInts.flatten)
    assert(opt.get === optInts.flatten.sum)
  }

  "A Sequence of Option[Long]" should "be summed by a Semigroup" in {
    val opt = sumOption(optLongs.flatten)
    assert(opt.get === optLongs.flatten.sum)
  }

  "A Sequence of Option[String]" should "be summed by a Semigroup" in {
    val opt = sumOption(optStrs.flatten)
    assert(opt === optStr)
  }

  "A Sequence of Either[String, BigDecimal]" should "be summed by a Semigroup" in {
    val eith = sumOption(eithBigDecs).get
    assert(eith.right.get === filterRight(eithBigDecs).sum)
  }

  "A Sequence of Either[String, BigInt]" should "be summed by a Semigroup" in {
    val eith = sumOption(eithBigInts).get
    assert(eith.right.get === filterRight(eithBigInts).sum)
  }

  "A Sequence of Either[String, Boolean]" should "be summed by a Semigroup" in {
    val eith = sumOption(eithBooleans).get
    assert(eith === Right(true))
  }

  "A Sequence of Either[String, Double]" should "be summed by a Semigroup" in {
    val eith = sumOption(eithDoubles).get
    assert(eith.right.get === filterRight(eithDoubles).sum)
  }

  "A Sequence of Either[String, Float]" should "be summed by a Semigroup" in {
    val eith = sumOption(eithFloats).get
    assert(eith.right.get === filterRight(eithFloats).sum)
  }

  "A Sequence of Either[String, Int]" should "be summed by a Semigroup" in {
    val eith = sumOption(eithInts).get
    assert(eith.right.get === filterRight(eithInts).sum)
  }

  "A Sequence of Either[String, Long]" should "be summed by a Semigroup" in {
    val eith = sumOption(eithLongs).get
    assert(eith.right.get === filterRight(eithLongs).sum)
  }

  "A Sequence of Either[String, String]" should "be summed by a Semigroup" in {
    val eith = sumOption(eithStrs).get
    assert(eith === eithStr)
  }
}
