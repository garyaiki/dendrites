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
class MaxSpec extends FlatSpecLike with TestValuesBuilder {

  "A Sequence of BigDecimal" should "return its Max" in {
    assert(max(bigDecimals) === bigDecimals.max)
  }

  "A Sequence of BigInt" should "return its Max" in {
    assert(max(bigInts) === bigInts.max)
  }

  "A Sequence of Boolean" should "return its Max" in {
    assert(max(booleans) === true)
  }

  "A Sequence of Double" should "return its Max" in {
    assert(max(doubles) === doubles.max)
  }

  "A Sequence of Float" should "return its Max" in {
    assert(max(floats) === floats.max)
  }

  "A Sequence of Int" should "return its Max" in {
    assert(max(ints) === ints.max)
  }

  "A Sequence of Long" should "return its Max" in {
    assert(max(longs) === longs.max)
  }

  "A Sequence of String" should "return its Max" in {
    assert(max(strings) === strings.max)
  }

  "A Sequence of Option[BigDecimal]" should "return its Max" in {
    assert(max(optBigDecs.flatten) === optBigDecs.flatten.max)
  }

  "A Sequence of Option[BigInt]" should "return its Max" in {
    assert(max(optBigInts.flatten) === optBigInts.flatten.max)
  }

  "A Sequence of Option[Boolean]" should "return its Max" in {
    assert(max(optBooleans.flatten) === true)
  }

  "A Sequence of Option[Double]" should "return its Max" in {
    assert(max(optDoubles.flatten) === optDoubles.flatten.max)
  }

  "A Sequence of Option[Float]" should "return its Max" in {
    assert(max(optFloats.flatten) === optFloats.flatten.max)
  }

  "A Sequence of Option[Int]" should "return its Max" in {
    assert(max(optInts.flatten) === optInts.flatten.max)
  }

  "A Sequence of Option[Long]" should "return its Max" in {
    assert(max(optLongs.flatten) === optLongs.flatten.max)
  }

  "A Sequence of Option[String]" should "return its Max" in {
    assert(max(optStrs.flatten) === optStrs.flatten.max)
  }

  "A Sequence of Either[String, BigDecimal]" should "return its Max" in {
    assert(max(filterRight(eithBigDecs)) === bigDecimals.max)
  }

  "A Sequence of Either[String, BigInt]" should "return its Max" in {
    assert(max(filterRight(eithBigInts)) === bigInts.max)
  }

  "A Sequence of Either[String, Boolean]" should "return its Max" in {
    assert(max(filterRight(eithBooleans)) === booleans.max)
  }

  "A Sequence of Either[String, Double]" should "return its Max" in {
    assert(max(filterRight(eithDoubles)) === doubles.max)
  }

  "A Sequence of Either[String, Float]" should "return its Max" in {

  }

  "A Sequence of Either[String, Int]" should "return its Max" in {
    assert(max(filterRight(eithInts)) === ints.max)
  }

  "A Sequence of Either[String, Long]" should "return its Max" in {
    assert(max(filterRight(eithLongs)) === longs.max)
  }

  "A Sequence of Either[String, String]" should "return its Max" in {
    assert(max(filterRight(eithStrs)) === strings.max)
  }
}
