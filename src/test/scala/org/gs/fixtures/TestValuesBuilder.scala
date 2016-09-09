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
package org.gs.fixtures

import org.scalatest.{Outcome, TestSuite, TestSuiteMixin}
import scala.annotation.implicitNotFound
import scala.collection.immutable.NumericRange
import org.gs.filters.extractElementByIndex

/**
  *
  * @author Gary Struthers
  */
trait TestValuesBuilder extends TestSuiteMixin { this: TestSuite =>

  abstract override def withFixture(test: NoArgTest): Outcome = {
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

  val bigDecimals = extractElementByIndex[BigDecimal](keyBigDecimal, 1)
  val bigInts = extractElementByIndex[BigInt](keyBigInt, 1)
  val booleans = extractElementByIndex[Boolean](keyBoolean, 1)
  val doubles = extractElementByIndex[Double](keyDouble, 1)
  val floats = extractElementByIndex[Float](keyFloat, 1)
  val ints = extractElementByIndex[Int](keyInt, 1)
  val longs = extractElementByIndex[Long](keyLong, 1)
  val strings = extractElementByIndex[String](keyString, 1)

  val optBigDecs = extractElementByIndex[Option[BigDecimal]](keyOptBigDec, 1)
  val optBigInts = extractElementByIndex[Option[BigInt]](keyOptBigInt, 1)
  val optBooleans = extractElementByIndex[Option[Boolean]](keyOptBool, 1)
  val optDoubles = extractElementByIndex[Option[Double]](keyOptDouble, 1)
  val optFloats = extractElementByIndex[Option[Float]](keyOptFloat, 1)
  val optInts = extractElementByIndex[Option[Int]](keyOptInt, 1)
  val optLongs = extractElementByIndex[Option[Long]](keyOptLong, 1)
  val optStrs = extractElementByIndex[Option[String]](keyOptStr, 1)

  val eithBigDecs = extractElementByIndex[Either[String, BigDecimal]](keyEithBigDec, 1)
  val eithBigInts = extractElementByIndex[Either[String, BigInt]](keyEithBigInt, 1)
  val eithBooleans = extractElementByIndex[Either[String, Boolean]](keyEithBool, 1)
  val eithDoubles = extractElementByIndex[Either[String, Double]](keyEithDouble, 1)
  val eithFloats = extractElementByIndex[Either[String, Float]](keyEithFloat, 1)
  val eithInts = extractElementByIndex[Either[String, Int]](keyEithInt, 1)
  val eithLongs = extractElementByIndex[Either[String, Long]](keyEithLong, 1)
  val eithStrs = extractElementByIndex[Either[String, String]](keyEithStr, 1)
  
  // 'a' - 32 = 'A'
  val bigDecimals2 = bigDecimals.map(x => x - 32)
  val bigInts2 = bigInts.map(x => x - 32)
  val doubles2 = doubles.map(x => x - 32)
  val floats2 = floats.map(x => x - 32)
  val ints2 = ints.map(x => x - 32)
  val longs2 = longs.map(x => x - 32)

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
