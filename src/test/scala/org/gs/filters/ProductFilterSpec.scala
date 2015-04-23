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
  
  "A Product Filter" should "return all BigDecimal fields from a sequence of case classes" in {
    val caseClasses = keyBigDecimal.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isBigDecimal)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isBigDecimal))
  }
  
  it should "return all BigInt fields from a sequence of case classes" in {
    val caseClasses = keyBigInt.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isBigInt)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isBigInt))
  }  

  it should "return all Boolean fields from a sequence of case classes" in {
    val caseClasses = keyBoolean.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isBoolean)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isBoolean))
  }

  it should "return all Double fields from a sequence of case classes" in {
    val caseClasses = keyDouble.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isDouble)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isDouble))
  }

  it should "return all Float fields from a sequence of case classes" in {
    val caseClasses = keyFloat.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isFloat)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isFloat))
  }

  it should "return all Int fields from a sequence of case classes" in {
    val caseClasses = keyInt.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isInt)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isInt))
  }

  it should "return all Long fields from a sequence of case classes" in {
    val caseClasses = keyLong.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isLong)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isLong))
  }

  it should "return all String fields from a sequence of case classes" in {
    val caseClasses = keyString.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isString)
    assert(caseClasses.size * 2 === filtered.size)
    assert(filtered.forall(isString))
  }
  
  it should "return all Option BigDecimal fields from a sequence of case classes" in {
    val caseClasses = keyOptBigDec.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isOptionBigDecimal)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isOptionBigDecimal))
  }
  
  it should "return all Option BigInt fields from a sequence of case classes" in {
    val caseClasses = keyOptBigInt.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isOptionBigInt)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isOptionBigInt))
  }  

  it should "return all Option Boolean fields from a sequence of case classes" in {
    val caseClasses = keyOptBool.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isOptionBoolean)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isOptionBoolean))
  }

  it should "return all Option Double fields from a sequence of case classes" in {
    val caseClasses = keyOptDouble.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isOptionDouble)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isOptionDouble))
  }

  it should "return all Option Float fields from a sequence of case classes" in {
    val caseClasses = keyOptFloat.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isOptionFloat)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isOptionFloat))
  }

  it should "return all Option Int fields from a sequence of case classes" in {
    val caseClasses = keyOptInt.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isOptionInt)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isOptionInt))
  }

  it should "return all Option Long fields from a sequence of case classes" in {
    val caseClasses = keyOptLong.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isOptionLong)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isOptionLong))
  }

  it should "return all Option String fields from a sequence of case classes" in {
    val caseClasses = keyOptStr.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isOptionString)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isOptionString))
  }
  
  it should "return all Either BigDecimal fields from a sequence of case classes" in {
    val caseClasses = keyEithBigDec.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isEitherStringBigDecimal)
    assert(caseClasses.size === filtered.size)
  //  val cast = filtered.asInstanceOf[IndexedSeq[Either[String, BigDecimal]]]
  //  val rights = filterRight[String, BigDecimal](cast)
    assert(filtered.forall(isType[Right[String,BigDecimal]]))
  }
  
  it should "return all Either BigInt fields from a sequence of case classes" in {
    val caseClasses = keyEithBigInt.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isEitherStringBigInt)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isEitherStringBigInt))
  }  

  it should "return all Either Boolean fields from a sequence of case classes" in {
    val caseClasses = keyEithBool.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isEitherStringBoolean)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isEitherStringBoolean))
  }

  it should "return all Either Double fields from a sequence of case classes" in {
    val caseClasses = keyEithDouble.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isEitherStringDouble)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isEitherStringDouble))
  }

  it should "return all Either Float fields from a sequence of case classes" in {
    val caseClasses = keyEithFloat.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isEitherStringFloat)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isEitherStringFloat))
  }

  it should "return all Either Int fields from a sequence of case classes" in {
    val caseClasses = keyEithInt.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isEitherStringInt)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isEitherStringInt))
  }

  it should "return all Either Long fields from a sequence of case classes" in {
    val caseClasses = keyEithLong.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isEitherStringLong)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isEitherStringLong))
  }

  it should "return all Either String fields from a sequence of case classes" in {
    val caseClasses = keyEithStr.toIndexedSeq
    val filtered = filterProducts(caseClasses, productFilter, isEitherStringString)
    assert(caseClasses.size === filtered.size)
    assert(filtered.forall(isEitherStringString))
  }

}
