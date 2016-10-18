/** Copyright 2016 Gary Struthers

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
package com.github.garyaiki.dendrites.filters

import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder

/**
  *
  * @author Gary Struthers
  *
  */
class ProductFilterSpec extends WordSpecLike with TestValuesBuilder {

  val mixCaseClasses = keyBigDecimal ++ keyBigInt ++ keyBoolean ++ keyDouble ++ keyFloat ++
    keyInt ++ keyLong ++ keyString ++ keyOptBigDec ++ keyOptBigInt ++ keyOptBool ++ keyOptDouble ++
    keyOptFloat ++ keyOptInt ++ keyOptLong ++ keyOptStr ++ keyEithBigDec ++ keyEithBigInt ++
    keyEithBool ++ keyEithDouble ++ keyEithFloat ++ keyEithInt ++ keyEithLong ++ keyEithStr

  "A Product Filter" should {
    "return all BigDecimal fields from a sequence of case classes" in {
    	val filtered = filterProducts(mixCaseClasses, fieldFilter, isType[BigDecimal])
    			val flat = filtered
    			val dbug = filtered.find { x => !x.isInstanceOf[BigDecimal] }
    	keyBigDecimal.size shouldBe filtered.size
    	assert(filtered.forall(isType[BigDecimal]))
    }
  }

  it should {
    "return all BigInt fields from a sequence of case classes" in {
    	val filtered = filterProducts(mixCaseClasses, fieldFilter, isType[BigInt])
    	keyBigInt.size shouldBe filtered.size
    	assert(filtered.forall(isType[BigInt]))
    }
  }

  it should {
    "return all Boolean fields from a sequence of case classes" in {
    	val filtered = filterProducts(mixCaseClasses, fieldFilter, isType[Boolean])
    	keyBoolean.size shouldBe filtered.size
    	assert(filtered.forall(isType[Boolean]))
    }
  }

  it should {
    "return all Double fields from a sequence of case classes" in {
    	val filtered = filterProducts(mixCaseClasses, fieldFilter, isType[Double])
    	keyDouble.size shouldBe filtered.size
    	assert(filtered.forall(isType[Double]))
    }
  }

  it should {
    "return all Float fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isType[Float])
      keyFloat.size shouldBe filtered.size
      assert(filtered.forall(isType[Float]))
    }
  }

  it should {
    "return all Int fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isType[Int])
      keyInt.size shouldBe filtered.size
      assert(filtered.forall(isType[Int]))
    }
  }

  it should {
    "return all Long fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isType[Long])
      keyLong.size shouldBe filtered.size
      assert(filtered.forall(isType[Long]))
    }
  }

  it should {
    "return all String fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isType[String])
      keyString.size * 25 shouldBe filtered.size
      assert(filtered.forall(isType[String]))
    }
  }

  it should {
    "return all Option BigInt fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isOptionType[BigInt])
      keyOptBigInt.size shouldBe filtered.size
      assert(filtered.forall(isOptionType[BigInt]))
    }
  }

  it should {
    "return all Option Boolean fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isOptionType[Boolean])
      keyOptBool.size shouldBe filtered.size
      assert(filtered.forall(isOptionType[Boolean]))
    }
  }

  it should {
    "return all Option Double fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isOptionType[Double])
      keyOptDouble.size shouldBe filtered.size
      assert(filtered.forall(isOptionType[Double]))
    }
  }

  it should {
    "return all Option Float fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isOptionType[Float])
      keyOptFloat.size shouldBe filtered.size
      assert(filtered.forall(isOptionType[Float]))
    }
  }

  it should {
    "return all Option Int fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isOptionType[Int])
      keyOptInt.size shouldBe filtered.size
      assert(filtered.forall(isOptionType[Int]))
    }
  }

  it should {
    "return all Option Long fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isOptionType[Long])
      keyOptLong.size shouldBe filtered.size
      assert(filtered.forall(isOptionType[Long]))
    }
  }

  it should {
    "return all Option String fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isOptionType[String])
      keyOptStr.size shouldBe filtered.size
      assert(filtered.forall(isOptionType[String]))
    }
  }

  it should {
    "return all Either BigDecimal fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isEitherStringRight[BigDecimal])
      keyEithBigDec.size shouldBe filtered.size
      assert(filtered.forall(isEitherStringRight[BigDecimal]))
    }
  }

  it should {
    "return all Either BigInt fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isEitherStringRight[BigInt])
      keyEithBigInt.size shouldBe filtered.size
      assert(filtered.forall(isEitherStringRight[BigInt]))
    }
  }

  it should {
    "return all Either String fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isEitherStringRight[String])
      keyEithStr.size shouldBe filtered.size
      assert(filtered.forall(isEitherStringRight[String]))
    }
  }

  it should {
    "return all Either Long fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isEitherStringRight[Long])
      keyEithLong.size shouldBe filtered.size
      assert(filtered.forall(isEitherStringRight[Long]))
    }
  }

  it should {
    "return all Either Double fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isEitherStringRight[Double])
      keyEithDouble.size shouldBe filtered.size
      assert(filtered.forall(isEitherStringRight[Double]))
    }
  }

  it should {
    "return all Either Boolean fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isEitherStringRight[Boolean])
      keyEithBool.size shouldBe filtered.size
      assert(filtered.forall(isEitherStringRight[Boolean]))
    }
  }

  it should {
    "return all Either Float fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isEitherStringRight[Float])
      keyEithFloat.size shouldBe filtered.size
      assert(filtered.forall(isEitherStringRight[Float]))
    }
  }

  it should {
    "return all Either Int fields from a sequence of case classes" in {
      val filtered = filterProducts(mixCaseClasses, fieldFilter, isEitherStringRight[Int])
      keyEithInt.size shouldBe filtered.size
      assert(filtered.forall(isEitherStringRight[Int]))
    }
  }
}
