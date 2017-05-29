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
class MinSpec extends FlatSpecLike with TestValuesBuilder {

  "A Sequence of BigDecimal" should "return its Min" in { assert(min(bigDecimals) === bigDecimals.min) }

  "A Sequence of BigInt" should "return its Min" in { assert(min(bigInts) === bigInts.min) }

  "A Sequence of Boolean" should "return its Min" in { assert(min(booleans) === false) }

  "A Sequence of Double" should "return its Min" in { assert(min(doubles) === doubles.min) }

  "A Sequence of Float" should "return its Min" in { assert(min(floats) === floats.min) }

  "A Sequence of Int" should "return its Min" in { assert(min(ints) === ints.min) }

  "A Sequence of Long" should "return its Min" in { assert(min(longs) === longs.min) }

  "A Sequence of String" should "return its Min" in { assert(min(strings) === strings.min) }

  "A Sequence of Option[BigDecimal]" should "return its Min" in {
    assert(min(optBigDecs.flatten) === optBigDecs.flatten.min)
  }

  "A Sequence of Option[BigInt]" should "return its Min" in {
    assert(min(optBigInts.flatten) === optBigInts.flatten.min)
  }

  "A Sequence of Option[Boolean]" should "return its Min" in {
    assert(min(optBooleans.flatten) === false)
  }

  "A Sequence of Option[Double]" should "return its Min" in {
    assert(min(optDoubles.flatten) === optDoubles.flatten.min)
  }

  "A Sequence of Option[Float]" should "return its Min" in {
    assert(min(optFloats.flatten) === optFloats.flatten.min)
  }

  "A Sequence of Option[Int]" should "return its Min" in {
    assert(min(optInts.flatten) === optInts.flatten.min)
  }

  "A Sequence of Option[Long]" should "return its Min" in {
    assert(min(optLongs.flatten) === optLongs.flatten.min)
  }

  "A Sequence of Option[String]" should "return its Min" in {
    assert(min(optStrs.flatten) === optStrs.flatten.min)
  }

  "A Sequence of Either[String, BigDecimal]" should "return its Min" in {
    assert(min(filterRight(eithBigDecs)) === bigDecimals.min)
  }

  "A Sequence of Either[String, BigInt]" should "return its Min" in {
    assert(min(filterRight(eithBigInts)) === bigInts.min)
  }

  "A Sequence of Either[String, Boolean]" should "return its Min" in {
    assert(min(filterRight(eithBooleans)) === booleans.min)
  }

  "A Sequence of Either[String, Double]" should "return its Min" in {
    assert(min(filterRight(eithDoubles)) === doubles.min)
  }

  "A Sequence of Either[String, Float]" should "return its Min" in {
    assert(min(filterRight(eithFloats)) === floats.min)
  }

  "A Sequence of Either[String, Int]" should "return its Min" in {
    assert(min(filterRight(eithInts)) === ints.min)
  }

  "A Sequence of Either[String, Long]" should "return its Min" in {
    assert(min(filterRight(eithLongs)) === longs.min)
  }

  "A Sequence of Either[String, String]" should "return its Min" in {
    assert(min(filterRight(eithStrs)) === strings.min)
  }
}
