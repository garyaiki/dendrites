/**
  */
package org.gs.algebird

import org.scalatest.FlatSpecLike
import org.gs._
import org.gs.fixtures.{CaseClassLike, TestValuesBuilder}

/** @author garystruthers
  *
  */
class RingSpec extends FlatSpecLike with TestValuesBuilder {

  "A BigDecimal" should "be multiplied by a Ring" in {
    assert(times(bigDecimal, bigDecimal) === bigDecimal * bigDecimal)
  }

  "A BigInt" should "be multiplied by a Ring" in {
    assert(times(bigInt, bigInt) === bigInt * bigInt)
  }
/*
  "A Boolean" should "be multiplied by a Ring" in {
    val sum = sumOption(booleans)
    assert(sum.get === true)
  }
*/
  "A Double" should "be multiplied by a Ring" in {
    assert(times(double, double) === double * double)
  }

  "A Float" should "be multiplied by a Ring" in {
    assert(times(float, float) === float * float)
  }

  "A Int" should "be multiplied by a Ring" in {
    assert(times(int, int) === int * int)
  }

  "A Long" should "be multiplied by a Ring" in {
    assert(times(long, long) === long * long)
  }

  "A Option[BigDecimal]" should "be multiplied by a Ring" in {
    assert(times(optBigDec.get, optBigDec.get) === optBigDec.get * optBigDec.get)
  }

  "A Option[BigInt]" should "be multiplied by a Ring" in {
    assert(times(optBigInt.get, optBigInt.get) === optBigInt.get * optBigInt.get)
  }
/*
  "A Option[Boolean]" should "be multiplied by a Ring" in {
    val sum = sumOption(optBooleans.flatten)
    assert(sum.get === true)
  }
*/
  "A Option[Double]" should "be multiplied by a Ring" in {
    assert(times(optDouble.get, optDouble.get) === optDouble.get * optDouble.get)
  }

  "A Option[Float]" should "be multiplied by a Ring" in {
    assert(times(optFloat.get, optFloat.get) === optFloat.get * optFloat.get)
  }

  "A Option[Int]" should "be multiplied by a Ring" in {
    assert(times(optInt.get, optInt.get) === optInt.get * optInt.get)
  }

  "A Option[Long]" should "be multiplied by a Ring" in {
    assert(times(optLong.get, optLong.get) === optLong.get * optLong.get)
  }

  "A Either[String, BigDecimal]" should "be multiplied by a Ring" in {
    assert(times(eithBigDec.right.get, eithBigDec.right.get) ===
      eithBigDec.right.get * eithBigDec.right.get)
  }

  "A Either[String, BigInt]" should "be multiplied by a Ring" in {
    assert(times(eithBigInt.right.get, eithBigInt.right.get) ===
      eithBigInt.right.get * eithBigInt.right.get)
  }

  "A Either[String, Double]" should "be multiplied by a Ring" in {
    assert(times(eithDouble.right.get, eithDouble.right.get) ===
      eithDouble.right.get * eithDouble.right.get)
  }

  "A Either[String, Float]" should "be multiplied by a Ring" in {
    assert(times(eithFloat.right.get, eithFloat.right.get) ===
      eithFloat.right.get * eithFloat.right.get)
  }

  "A Either[String, Int]" should "be multiplied by a Ring" in {
    assert(times(eithInt.right.get, eithInt.right.get) ===
      eithInt.right.get * eithInt.right.get)
  }

  "A Either[String, Long]" should "be multiplied by a Ring" in {
    assert(times(eithLong.right.get, eithLong.right.get) ===
      eithLong.right.get * eithLong.right.get)
  }

  "A Sequence of BigDecimal" should "get a product from a Ring " in {
    assert(product(bigDecimals) === bigDecimals.product)
  }

  "A Sequence of BigInt" should "get a product from a Ring " in {
    assert(product(bigInts) === bigInts.product)
  }

  "A Sequence of Double" should "get a product from a Ring " in {
    assert(product(doubles) === doubles.product)
  }

  "A Sequence of Float" should "get a product from a Ring " in {
    assert(product(floats) === floats.product)
  }

  "A Sequence of Int" should "get a product from a Ring " in {
    assert(product(ints) === ints.product)
  }

  "A Sequence of Long" should "get a product from a Ring " in {
    assert(product(longs) === longs.product)
  }

  "A Sequence of Option[BigDecimal]" should "get a product from a Ring " in {
    assert(product(optBigDecs.flatten) === optBigDecs.flatten.product)
  }

  "A Sequence of Option[BigInt]" should "get a product from a Ring " in {
    assert(product(optBigInts.flatten) === optBigInts.flatten.product)
  }

  "A Sequence of Option[Double]" should "get a product from a Ring " in {
    assert(product(optDoubles.flatten) === optDoubles.flatten.product)
  }

  "A Sequence of Option[Float]" should "get a product from a Ring " in {
    assert(product(optFloats.flatten) === optFloats.flatten.product)
  }

  "A Sequence of Option[Int]" should "get a product from a Ring " in {
    assert(product(optInts.flatten) === optInts.flatten.product)
  }

  "A Sequence of Option[Long]" should "get a product from a Ring " in {
    assert(product(optLongs.flatten) === optLongs.flatten.product)
  }

  "A Sequence of Either[String, BigDecimal]" should "get a product from a Ring " in {
    assert(product(filterRight(eithBigDecs)) === filterRight(eithBigDecs).product)
  }

  "A Sequence of Either[String, BigInt]" should "get a product from a Ring " in {
    assert(product(filterRight(eithBigInts)) === filterRight(eithBigInts).product)
  }

  "A Sequence of Either[String, Double]" should "get a product from a Ring " in {
    assert(product(filterRight(eithDoubles)) === filterRight(eithDoubles).product)
  }

  "A Sequence of Either[String, Float]" should "get a product from a Ring " in {
    assert(product(filterRight(eithFloats)) === filterRight(eithFloats).product)
  }

  "A Sequence of Either[String, Int]" should "get a product from a Ring " in {
    assert(product(filterRight(eithInts)) === filterRight(eithInts).product)
  }

  "A Sequence of Either[String, Long]" should "get a product from a Ring " in {
    assert(product(filterRight(eithLongs)) === filterRight(eithLongs).product)
  }
}
