/**

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.github.garyaiki.dendrites.algebird

import org.scalatest.FlatSpecLike
import com.twitter.algebird.Operators
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder

/**
  *
  * @author Gary Struthers
  */
class FieldSpec extends FlatSpecLike with TestValuesBuilder {

  "A BigDecimal" should "get its recipricol from a Field" in { assert(inverse(bigDecimal) === 1/bigDecimal) }

  "A BigInt" should "get its recipricol from a Field" in { assert(inverse(bigInt) === 1/bigInt) }

  "A Double" should "get its recipricol from a Field" in { assert(inverse(double) === 1/double) }

  "A Float" should "get its recipricol from a Field" in { assert(inverse(float) === 1/float) }

  "A Int" should "get its recipricol from a Field" in { assert(inverse(int) === 1/int) }

  "A Long" should "get its recipricol from a Field" in { assert(inverse(long) === 1/long) }

  "A Option[BigDecimal]" should "get its recipricol from a Field" in {
    val v = Some(bigDecimal)
    assert(inverse(v.get) === 1/bigDecimal)
  }

  "A Option[BigInt]" should "get its recipricol from a Field" in {
    val v = Some(bigInt)
    assert(inverse(v.get) === 1/bigInt)
  }

  "A Option[Double]" should "get its recipricol from a Field" in {
    val v = Some(double)
    assert(inverse(v.get) === 1/double)
  }

  "A Option[Float]" should "get its recipricol from a Field" in {
    val v = Some(float)
    assert(inverse(v.get) === 1/float)
  }

  "A Option[Int]" should "get its recipricol from a Field" in {
    val v = Some(int)
    assert(inverse(v.get) === 1/int)
  }

  "A Option[Long]" should "get its recipricol from a Field" in {
    val v = Some(long)
    assert(inverse(v.get) === 1/long)
  }

  "A Either[String, BigDecimal]" should "get its recipricol from a Field" in {
    val v = Right(bigDecimal)
    assert(inverse(v.right.get) === 1/bigDecimal)
  }

  "A Either[String, BigInt]" should "get its recipricol from a Field" in {
    val v = Right(bigInt)
    assert(inverse(v.right.get) === 1/bigInt)
  }

  "A Either[String, Double]" should "get its recipricol from a Field" in {
    val v = Right(double)
    assert(inverse(v.right.get) === 1/double)
  }

  "A Either[String, Float]" should "get its recipricol from a Field" in {
    val v = Right(float)
    assert(inverse(v.right.get) === 1/float)
  }

  "A Either[String, Int]" should "get its recipricol from a Field" in {
    val v = Right(int)
    assert(inverse(v.right.get) === 1/int)
  }

  "A Either[String, Long]" should "get its recipricol from a Field" in {
    val v = Right(long)
    assert(inverse(v.right.get) === 1/long)
  }

  val op = Operators
  "A BigDecimal" should "be divided by a Group" in {
    assert(div(bigDecimal, bigDecimal) === 1)
  }

  "A BigInt" should "be divided by a Group" in {
    assert(div(bigInt, bigInt) === 1)
  }

  "A Double" should "be divided by a Group" in {
    assert(div(double, double) === 1.0)
  }

  "A Float" should "be divided by a Group" in {
    assert(div(float, float) === 1.0f)
  }

  "A Int" should "be divided by a Group" in {
    assert(div(int, int) === 1)
  }

  "A Long" should "be divided by a Group" in {
    assert(div(long, long) === 1L)
  }

  "A Option[BigDecimal]" should "be divided by a Group" in {
    assert(div(optBigDec.get, optBigDec.get) === 1)
  }

  "A Option[BigInt]" should "be divided by a Group" in {
    assert(div(optBigInt.get, optBigInt.get) === 1)
  }

  "A Option[Double]" should "be subtracted by a Group" in {
    assert(div(optDouble.get, optDouble.get) === 1.0)
  }

  "A Option[Float]" should "be divided by a Group" in {
    assert(div(optFloat.get, optFloat.get) === 1.0f)
  }

  "A Option[Int]" should "be divided by a Group" in {
    assert(div(optInt.get, optInt.get) === 1)
  }

  "A Option[Long]" should "be subtracted by a Group" in {
    assert(div(optLong.get, optLong.get) === 1L)
  }

  "A Either[String, BigDecimal]" should "be subtracted by a Group" in {
    assert(div(eithBigDec.right.get, eithBigDec.right.get) === 1)
  }

  "A Either[String, BigInt]" should "be subtracted by a Group" in {
    assert(div(eithBigInt.right.get, eithBigInt.right.get) === 1)
  }

  "A Either[String, Double]" should "be subtracted by a Group" in {
    assert(div(eithDouble.right.get, eithDouble.right.get) === 1.0)
  }

  "A Either[String, Float]" should "be subtracted by a Group" in {
    assert(div(eithFloat.right.get, eithFloat.right.get) === 1.0f)
  }

  "A Either[String, Int]" should "be subtracted by a Group" in {
    assert(div(eithInt.right.get, eithInt.right.get) === 1)
  }

  "A Either[String, Long]" should "be subtracted by a Group" in {
    assert(div(eithLong.right.get, eithLong.right.get) === 1L)
  }
}
