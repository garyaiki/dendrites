/**
  */
package org.gs

import org.gs._
import scala.collection.immutable.NumericRange
import org.scalatest._

/** @author garystruthers
  *
  */
trait TestValuesBuilder extends SuiteMixin { this: Suite =>

  abstract override def withFixture(test: NoArgTest) = {
    super.withFixture(test)
  }
  def makeCaseClasses[A, B](keyRange: NumericRange.Inclusive[Char])(
    implicit ev: CaseClassLike[A, B]): Seq[B] = {
    for (k <- keyRange) yield {
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

  val keyOptBigDec = makeCaseClasses[Option[BigDecimal], KeyOptBigDec](keyRange)
  val keyOptBigInt = makeCaseClasses[Option[BigInt], KeyOptBigInt](keyRange)
  val keyOptBool = makeCaseClasses[Option[Boolean], KeyOptBool](keyRange)
  val keyOptDouble = makeCaseClasses[Option[Double], KeyOptDouble](keyRange)
  val keyOptFloat = makeCaseClasses[Option[Float], KeyOptFloat](keyRange)
  val keyOptInt = makeCaseClasses[Option[Int], KeyOptInt](keyRange)
  val keyOptLong = makeCaseClasses[Option[Long], KeyOptLong](keyRange)
  val keyOptStr = makeCaseClasses[Option[String], KeyOptStr](keyRange)

  val keyEithBigDec = makeCaseClasses[Either[String, BigDecimal], KeyEithBigDec](keyRange)
  val keyEithBigInt = makeCaseClasses[Either[String, BigInt], KeyEithBigInt](keyRange)
  val keyEithBool = makeCaseClasses[Either[String, Boolean], KeyEithBool](keyRange)
  val keyEithDouble = makeCaseClasses[Either[String, Double], KeyEithDouble](keyRange)
  val keyEithFloat = makeCaseClasses[Either[String, Float], KeyEithFloat](keyRange)
  val keyEithInt = makeCaseClasses[Either[String, Int], KeyEithInt](keyRange)
  val keyEithLong = makeCaseClasses[Either[String, Long], KeyEithLong](keyRange)
  val keyEithStr = makeCaseClasses[Either[String, String], KeyEithStr](keyRange)

  val bigDecimals = extractElement[BigDecimal](keyBigDecimal, 1)
  val bigInts = extractElement[BigInt](keyBigInt, 1)
  val booleans = extractElement[Boolean](keyBoolean, 1)
  val doubles = extractElement[Double](keyDouble, 1)
  val floats = extractElement[Float](keyFloat, 1)
  val ints = extractElement[Int](keyInt, 1)
  val longs = extractElement[Long](keyLong, 1)
  val strings = extractElement[String](keyString, 1)

  val optBigDecs = extractElement[Option[BigDecimal]](keyOptBigDec, 1)
  val optBigInts = extractElement[Option[BigInt]](keyOptBigInt, 1)
  val optBooleans = extractElement[Option[Boolean]](keyOptBool, 1)
  val optDoubles = extractElement[Option[Double]](keyOptDouble, 1)
  val optFloats = extractElement[Option[Float]](keyOptFloat, 1)
  val optInts = extractElement[Option[Int]](keyOptInt, 1)
  val optLongs = extractElement[Option[Long]](keyOptLong, 1)
  val optStrs = extractElement[Option[String]](keyOptStr, 1)

  val eithBigDecs = extractElement[Either[String, BigDecimal]](keyEithBigDec, 1)
  val eithBigInts = extractElement[Either[String, BigInt]](keyEithBigInt, 1)
  val eithBooleans = extractElement[Either[String, Boolean]](keyEithBool, 1)
  val eithDoubles = extractElement[Either[String, Double]](keyEithDouble, 1)
  val eithFloats = extractElement[Either[String, Float]](keyEithFloat, 1)
  val eithInts = extractElement[Either[String, Int]](keyEithInt, 1)
  val eithLongs = extractElement[Either[String, Long]](keyEithLong, 1)
  val eithStrs = extractElement[Either[String, String]](keyEithStr, 1)
  
  val bigDecimal = BigDecimal(2847)
  val bigInt = BigInt(2847)
  val double = 3130.0
  val float = 3131.7f
  val int = 2847
  val long = 2847L
  val string = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

  val optBigDec = Some(bigDecimal)
  val optBigInt = Some(bigInt)
  val optDouble = Some(double)
  val optFloat = Some(float)
  val optInt = Some(int)
  val optLong = Some(long)
  val optStr = Some(string)

  val eithBigDec = Right(bigDecimal)
  val eithBigInt = Right(bigInt)
  val eithDouble = Right(double)
  val eithFloat = Right(float)
  val eithInt = Right(int)
  val eithLong = Right(long)
  val eithStr = Right(string)
}