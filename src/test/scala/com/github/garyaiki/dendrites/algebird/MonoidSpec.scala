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

import org.scalatest.{FlatSpecLike, Matchers}
import com.github.garyaiki.dendrites.filters.filterRight
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder

/**
  *
  * @author Gary Struthers
  */
class MonoidSpec extends FlatSpecLike with TestValuesBuilder {

  "A Sequence of BigDecimal" should "be summed by a Monoid" in { assert(sum(bigDecimals) === bigDecimals.sum) }

  "A Sequence of BigInt" should "be summed by a Monoid" in { assert(sum(bigInts) === bigInts.sum) }

  "A Sequence of Boolean" should "be summed by a Monoid" in { assert(sum(booleans) === true) }

  "A Sequence of Double" should "be summed by a Monoid" in { assert(sum(doubles) === doubles.sum) }

  "A Sequence of Float" should "be summed by a Monoid" in { assert(sum(floats) === floats.sum) }

  "A Sequence of Int" should "be summed by a Monoid" in { assert(sum(ints) === ints.sum) }

  "A Sequence of Long" should "be summed by a Monoid" in { assert(sum(longs) === longs.sum) }

  "A Sequence of String" should "be summed by a Monoid" in { assert(sum(strings) === string) }

  "A Sequence of Option[BigDecimal]" should "be summed by a Monoid" in {
    assert(sum(optBigDecs.flatten) === optBigDecs.flatten.sum)
  }

  "A Sequence of Option[BigInt]" should "be summed by a Monoid" in {
    assert(sum(optBigInts.flatten) === optBigInts.flatten.sum)
  }

  "A Sequence of Option[Boolean]" should "be summed by a Monoid" in {
    assert(sum(optBooleans.flatten) === true)
  }

  "A Sequence of Option[Double]" should "be summed by a Monoid" in {
    assert(sum(optDoubles.flatten) === optDoubles.flatten.sum)
  }

  "A Sequence of Option[Float]" should "be summed by a Monoid" in {
    assert(sum(optFloats.flatten) === optFloats.flatten.sum)
  }

  "A Sequence of Option[Int]" should "be summed by a Monoid" in {
    assert(sum(optInts.flatten) === optInts.flatten.sum)
  }

  "A Sequence of Option[Long]" should "be summed by a Monoid" in {
    assert(sum(optLongs.flatten) === optLongs.flatten.sum)
  }

  "A Sequence of Option[String]" should "be summed by a Monoid" in {
    assert(sum(optStrs.flatten) === string)
  }

  "A Sequence of Either[String, BigDecimal]" should "be summed by a Monoid" in {
    assert(sum(eithBigDecs).right.get === filterRight(eithBigDecs).sum)
  }

  "A Sequence of Either[String, BigInt]" should "be summed by a Monoid" in {
    assert(sum(eithBigInts).right.get === filterRight(eithBigInts).sum)
  }

  "A Sequence of Either[String, Boolean]" should "be summed by a Monoid" in {
    val s = sum(eithBooleans)
    assert(s.right.get === true)
  }

  "A Sequence of Either[String, Double]" should "be summed by a Monoid" in {
    assert(sum(eithDoubles).right.get === filterRight(eithDoubles).sum)
  }

  "A Sequence of Either[String, Float]" should "be summed by a Monoid" in {
    assert(sum(eithFloats).right.get === filterRight(eithFloats).sum)
  }

  "A Sequence of Either[String, Int]" should "be summed by a Monoid" in {
    assert(sum(eithInts).right.get === filterRight(eithInts).sum)
  }

  "A Sequence of Either[String, Long]" should "be summed by a Monoid" in {
    assert(sum(eithLongs).right.get === filterRight(eithLongs).sum)
  }

  "A Sequence of Either[String, String]" should "be summed by a Monoid" in {
    assert(sum(eithStrs) === eithStr)
  }
}
