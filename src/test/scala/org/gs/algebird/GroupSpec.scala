/**
  */
package org.gs.algebird

import org.scalatest.FlatSpecLike
import org.gs.TestValuesBuilder


/** @author garystruthers
  *
  */
class GroupSpec extends FlatSpecLike with TestValuesBuilder {

  "A BigDecimal" should "be negated by a Group" in {
    assert(negate(bigDecimal * -1) === bigDecimal)
  }

  "A BigInt" should "be negated by a Group" in {
    assert(negate(bigInt * -1) === bigInt)
  }
/*
  "A Boolean" should "be negated by a Group" in {
    val sum = sumOption(booleans)
    assert(sum.get === true)
  }
*/
  "A Double" should "be negated by a Group" in {
    assert(negate(double * -1) === double)
  }

  "A Float" should "be negated by a Group" in {
    assert(negate(float * -1) === float)
  }

  "A Int" should "be negated by a Group" in {
    assert(negate(int * -1) === int)
  }

  "A Long" should "be negated by a Group" in {
    assert(negate(long * -1) === long)
  }

  "A Option[BigDecimal]" should "be negated by a Group" in {
    val v = Some(bigDecimal * -1)
    assert(negate(v.get) === bigDecimal)
  }

  "A Option[BigInt]" should "be negated by a Group" in {
    val v = Some(bigInt * -1)
    assert(negate(v.get) === bigInt)
  }
/*
  "A Option[Boolean]" should "be negated by a Group" in {
    val sum = sumOption(optBooleans.flatten)
    assert(sum.get === true)
  }
*/
  "A Option[Double]" should "be negated by a Group" in {
    val v = Some(double * -1)
    assert(negate(v.get) === double)
  }

  "A Option[Float]" should "be negated by a Group" in {
    val v = Some(float * -1)
    assert(negate(v.get) === float)
  }

  "A Option[Int]" should "be negated by a Group" in {
    val v = Some(int * -1)
    assert(negate(v.get) === int)
  }

  "A Option[Long]" should "be negated by a Group" in {
    val v = Some(long * -1)
    assert(negate(v.get) === long)
  }

  "A Either[String, BigDecimal]" should "be negated by a Group" in {
    val v = Right(bigDecimal * -1)
    assert(negate(v.right.get) === bigDecimal)
  }

  "A Either[String, BigInt]" should "be negated by a Group" in {
    val v = Right(bigInt * -1)
    assert(negate(v.right.get) === bigInt)
  }

  "A Either[String, Double]" should "be negated by a Group" in {
    val v = Right(double * -1)
    assert(negate(v.right.get) === double)
  }

  "A Either[String, Float]" should "be negated by a Group" in {
    val v = Right(float * -1)
    assert(negate(v.right.get) === float)
  }

  "A Either[String, Int]" should "be negated by a Group" in {
    val v = Right(int * -1)
    assert(negate(v.right.get) === int)
  }

  "A Either[String, Long]" should "be negated by a Group" in {
    val v = Right(long * -1)
    assert(negate(v.right.get) === long)
  }

  "A BigDecimal" should "be subtracted by a Group" in {
    assert(minus(bigDecimal, bigDecimal) === 0)
  }

  "A BigInt" should "be subtracted by a Group" in {
    assert(minus(bigInt, bigInt) === 0)
  }
/*
  "A Boolean" should "be subtracted by a Group" in {
    val sum = sumOption(booleans)
    assert(sum.get === true)
  }
*/
  "A Double" should "be subtracted by a Group" in {
    assert(minus(double, double) === 0.0)
  }

  "A Float" should "be subtracted by a Group" in {
    assert(minus(float, float) === 0.0f)
  }

  "A Int" should "be subtracted by a Group" in {
    assert(minus(int, int) === 0)
  }

  "A Long" should "be subtracted by a Group" in {
    assert(minus(long, long) === 0L)
  }

  "A Option[BigDecimal]" should "be subtracted by a Group" in {
    assert(minus(optBigDec.get, optBigDec.get) === 0)
  }

  "A Option[BigInt]" should "be subtracted by a Group" in {
    assert(minus(optBigInt.get, optBigInt.get) === 0)
  }
/*
  "A Option[Boolean]" should "be subtracted by a Group" in {
    val sum = sumOption(optBooleans.flatten)
    assert(sum.get === true)
  }
*/
  "A Option[Double]" should "be subtracted by a Group" in {
    assert(minus(optDouble.get, optDouble.get) === 0.0)
  }

  "A Option[Float]" should "be subtracted by a Group" in {
    assert(minus(optFloat.get, optFloat.get) === 0.0f)
  }

  "A Option[Int]" should "be subtracted by a Group" in {
    assert(minus(optInt.get, optInt.get) === 0)
  }

  "A Option[Long]" should "be subtracted by a Group" in {
    assert(minus(optLong.get, optLong.get) === 0L)
  }

  "A Either[String, BigDecimal]" should "be subtracted by a Group" in {
    assert(minus(eithBigDec.right.get, eithBigDec.right.get) === 0)
  }

  "A Either[String, BigInt]" should "be subtracted by a Group" in {
    assert(minus(eithBigInt.right.get, eithBigInt.right.get) === 0)
  }

  "A Either[String, Double]" should "be subtracted by a Group" in {
    assert(minus(eithDouble.right.get, eithDouble.right.get) === 0.0)
  }

  "A Either[String, Float]" should "be subtracted by a Group" in {
    assert(minus(eithFloat.right.get, eithFloat.right.get) === 0.0f)
  }

  "A Either[String, Int]" should "be subtracted by a Group" in {
    assert(minus(eithInt.right.get, eithInt.right.get) === 0)
  }

  "A Either[String, Long]" should "be subtracted by a Group" in {
    assert(minus(eithLong.right.get, eithLong.right.get) === 0L)
  }
}