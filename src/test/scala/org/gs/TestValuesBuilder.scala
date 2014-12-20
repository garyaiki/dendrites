/**
  */
package org.gs

import org.gs._
import scala.collection.immutable.NumericRange

/** @author garystruthers
  *
  */
trait TestValuesBuilder {
  
  
  def makeCaseClasses[A, B](keyRange: NumericRange.Inclusive[Char])(implicit ev: CaseClassLike[A, B]
    ): Seq[B] = {
    for(k <- keyRange) yield {
      ev.apply(k)
    }
  }
  val keyRange = 'a' to 'z'
  val keyBigDecimal = makeCaseClasses[BigDecimal, KeyBigDecimal](keyRange)
  val keyBigInt = makeCaseClasses[BigInt, KeyBigInt](keyRange)
  val keyBoolean = makeCaseClasses[Boolean, KeyBoolean](keyRange)
  val keyDouble = makeCaseClasses[Double, KeyDouble](keyRange)
  val keyFloat = makeCaseClasses[Float, KeyFloat](keyRange)
  val keyInt = makeCaseClasses[Int, KeyInt](keyRange)
  val keyLong = makeCaseClasses[Long, KeyLong](keyRange)
  val keyString = makeCaseClasses[String, KeyString](keyRange)
  val bigDecimals = extractElement[BigDecimal](keyBigDecimal, 1)
  val bigInts = extractElement[BigInt](keyBigInt, 1)
  val doubles = extractElement[Double](keyDouble, 1)
  val floats = extractElement[Float](keyFloat, 1)
  val ints = extractElement[Int](keyInt, 1)
  val longs = extractElement[Long](keyLong, 1)
  val strings = extractElement[String](keyString, 1)

}