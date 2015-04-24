/** 
  * 
  */
package org.gs.filters

import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import org.gs.fixtures.TestValuesBuilder

/** @author garystruthers
  *
  */
class ProductFilterSpec extends FlatSpecLike with TestValuesBuilder {
  
  val mixCaseClasses = keyBigDecimal ++ keyBigInt ++ keyBoolean ++ keyDouble ++ keyFloat ++
    keyInt ++ keyLong ++ keyString ++ keyOptBigDec ++ keyOptBigInt ++ keyOptBool ++ keyOptDouble ++
    keyOptFloat ++ keyOptInt ++ keyOptLong ++ keyOptStr ++ keyEithBigDec ++ keyEithBigInt ++
    keyEithBool ++ keyEithDouble ++ keyEithFloat ++ keyEithInt ++ keyEithLong ++ keyEithStr
  
  "A Product Filter" should "return all BigDecimal fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isBigDecimal)
    assert(keyBigDecimal.size === filtered.size)
    assert(filtered.forall(isBigDecimal))
  }
  
  it should "return all BigInt fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isBigInt)
    assert(keyBigInt.size === filtered.size)
    assert(filtered.forall(isBigInt))
  }  

  it should "return all Boolean fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isBoolean)
    assert(keyBoolean.size === filtered.size)
    assert(filtered.forall(isBoolean))
  }

  it should "return all Double fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isDouble)
    assert(keyDouble.size === filtered.size)
    assert(filtered.forall(isDouble))
  }

  it should "return all Float fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isFloat)
    assert(keyFloat.size === filtered.size)
    assert(filtered.forall(isFloat))
  }

  it should "return all Int fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isInt)
    assert(keyInt.size === filtered.size)
    assert(filtered.forall(isInt))
  }

  it should "return all Long fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isLong)
    assert(keyLong.size === filtered.size)
    assert(filtered.forall(isLong))
  }

  it should "return all String fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isString)
    assert(keyString.size * 25 === filtered.size)
    assert(filtered.forall(isString))
  }
  
  it should "return all Option BigDecimal fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isOptionBigDecimal)
    assert(keyOptBigDec.size === filtered.size)
    assert(filtered.forall(isOptionBigDecimal))
  }
  
  it should "return all Option BigInt fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isOptionBigInt)
    assert(keyOptBigInt.size === filtered.size)
    assert(filtered.forall(isOptionBigInt))
  }  

  it should "return all Option Boolean fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isOptionBoolean)
    assert(keyOptBool.size === filtered.size)
    assert(filtered.forall(isOptionBoolean))
  }

  it should "return all Option Double fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isOptionDouble)
    assert(keyOptDouble.size === filtered.size)
    assert(filtered.forall(isOptionDouble))
  }

  it should "return all Option Float fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isOptionFloat)
    assert(keyOptFloat.size === filtered.size)
    assert(filtered.forall(isOptionFloat))
  }

  it should "return all Option Int fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isOptionInt)
    assert(keyOptInt.size === filtered.size)
    assert(filtered.forall(isOptionInt))
  }

  it should "return all Option Long fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isOptionLong)
    assert(keyOptLong.size === filtered.size)
    assert(filtered.forall(isOptionLong))
  }

  it should "return all Option String fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isOptionString)
    assert(keyOptStr.size === filtered.size)
    assert(filtered.forall(isOptionString))
  }
  
  it should "return all Either BigDecimal fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isEitherStringBigDecimal)
    assert(keyEithBigDec.size === filtered.size)
    assert(filtered.forall(isEitherStringBigDecimal))
  }
  
  it should "return all Either BigInt fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isEitherStringBigInt)
    assert(keyEithBigInt.size === filtered.size)
    assert(filtered.forall(isEitherStringBigInt))
  }  

  it should "return all Either Boolean fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isEitherStringBoolean)
    assert(keyEithBool.size === filtered.size)
    assert(filtered.forall(isEitherStringBoolean))
  }

  it should "return all Either Double fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isEitherStringDouble)
    assert(keyEithDouble.size === filtered.size)
    assert(filtered.forall(isEitherStringDouble))
  }

  it should "return all Either Float fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isEitherStringFloat)
    assert(keyEithFloat.size === filtered.size)
    assert(filtered.forall(isEitherStringFloat))
  }

  it should "return all Either Int fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isEitherStringInt)
    assert(keyEithInt.size === filtered.size)
    assert(filtered.forall(isEitherStringInt))
  }

  it should "return all Either Long fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isEitherStringLong)
    assert(keyEithLong.size === filtered.size)
    assert(filtered.forall(isEitherStringLong))
  }

  it should "return all Either String fields from a sequence of case classes" in {
    val filtered = filterProducts(mixCaseClasses, productFilter, isEitherStringString)
    assert(keyEithStr.size === filtered.size)
    assert(filtered.forall(isEitherStringString))
  }

}
