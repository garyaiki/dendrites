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
package com.github.garyaiki.dendrites.examples.fixtures

import annotation.implicitNotFound
import scala.math.BigDecimal.int2bigDecimal
import scala.math.BigInt.int2bigInt
import com.github.garyaiki.dendrites.fixtures.{CaseClassLike, KeyEithDouble, KeyEithFloat, KeyOptBigInt, KeyFloat,
  KeyBoolean, KeyEithBigDec, KeyEithBool, KeyDouble, KeyBigInt, KeyEithLong, KeyOptStr,KeyOptInt, KeyOptBool,
  KeyBigDecimal, KeyString, KeyEithStr, KeyEithInt, KeyOptDouble, KeyOptLong, KeyEithBigInt, KeyOptFloat, KeyLong,
  KeyInt, KeyOptBigDec}

@implicitNotFound(msg = "Cannot find AccountTypeLike type class for ${A}")
trait AccountTypeLike[A, B] {
  def apply(k: Char): B
}
/** @TODO not used delete
  * @author Gary Struthers
  *
  */
object AccountTypeLike {
  def altTF(k: Char): Boolean = if (k % 2 == 0) (true) else (false)

  implicit val mc = new java.math.MathContext(2)

  def exDouble(k: Char)(implicit ev: java.math.MathContext): Double = BigDecimal(k.toInt * 1.1, ev).toDouble
  def exFloat(k: Char): Float = k.toInt * 1.1f
  def exStr(k: Char): String = k.toString.toUpperCase()
  def exLeftStr(k: Char): String = s"Error key:$k"

  implicit object AccountTypeLikeBigDecimal extends AccountTypeLike[BigDecimal, KeyBigDecimal] {
    def apply(k: Char): KeyBigDecimal = KeyBigDecimal(k.toString, BigDecimal(k.toInt))
  }

  implicit object CaseClassLikeBigInt extends CaseClassLike[BigInt, KeyBigInt] {
    def apply(k: Char): KeyBigInt = KeyBigInt(k.toString, BigInt(k.toInt))
  }

  implicit object CaseClassLikeBoolean extends CaseClassLike[Boolean, KeyBoolean] {
    def apply(k: Char): KeyBoolean = KeyBoolean(k.toString, altTF(k))
  }

  implicit object CaseClassLikeDouble extends CaseClassLike[Double, KeyDouble] {
    def apply(k: Char): KeyDouble = KeyDouble(k.toString, exDouble(k))
  }

  implicit object CaseClassLikeFloat extends CaseClassLike[Float, KeyFloat] {
    def apply(k: Char): KeyFloat = KeyFloat(k.toString, exFloat(k))
  }

  implicit object CaseClassLikeInt extends CaseClassLike[Int, KeyInt] {
    def apply(k: Char): KeyInt = KeyInt(k.toString, k.toInt)
  }

  implicit object CaseClassLikeLong extends CaseClassLike[Long, KeyLong] {
    def apply(k: Char): KeyLong = KeyLong(k.toString, k.toLong)
  }

  implicit object CaseClassLikeString extends CaseClassLike[String, KeyString] {
    def apply(k: Char): KeyString = KeyString(k.toString, exStr(k))
  }

  implicit object CaseClassLikeOptBigDec extends CaseClassLike[Option[BigDecimal], KeyOptBigDec] {
    def apply(k: Char): KeyOptBigDec = KeyOptBigDec(k.toString, Some(BigDecimal(k.toInt)))
  }

  implicit object CaseClassLikeOptBigInt extends CaseClassLike[Option[BigInt], KeyOptBigInt] {
    def apply(k: Char): KeyOptBigInt = KeyOptBigInt(k.toString, Some(BigInt(k.toInt)))
  }
  implicit object CaseClassLikeOptBool extends CaseClassLike[Option[Boolean], KeyOptBool] {
    def apply(k: Char): KeyOptBool = KeyOptBool(k.toString, Some(altTF(k)))
  }

  implicit object CaseClassLikeOptDouble extends CaseClassLike[Option[Double], KeyOptDouble] {
    def apply(k: Char): KeyOptDouble = KeyOptDouble(k.toString, Some(exDouble(k)))
  }

  implicit object CaseClassLikeOptFloat extends CaseClassLike[Option[Float], KeyOptFloat] {
    def apply(k: Char): KeyOptFloat = KeyOptFloat(k.toString, Some(exFloat(k)))
  }

  implicit object CaseClassLikeOptInt extends CaseClassLike[Option[Int], KeyOptInt] {
    def apply(k: Char): KeyOptInt = KeyOptInt(k.toString, Some(k.toInt))
  }

  implicit object CaseClassLikeOptLong extends CaseClassLike[Option[Long], KeyOptLong] {
    def apply(k: Char): KeyOptLong = KeyOptLong(k.toString, Some(k.toLong))
  }

  implicit object CaseClassLikeOptStr extends CaseClassLike[Option[String], KeyOptStr] {
    def apply(k: Char): KeyOptStr = KeyOptStr(k.toString, Some(exStr(k)))
  }

  implicit object CaseClassLikeEithBigDec extends CaseClassLike[Either[String, BigDecimal], KeyEithBigDec] {
    def apply(k: Char): KeyEithBigDec = KeyEithBigDec(k.toString, Right(BigDecimal(k.toInt)))
  }

  implicit object CaseClassLikeEithBigInt extends CaseClassLike[Either[String, BigInt], KeyEithBigInt] {
    def apply(k: Char): KeyEithBigInt = KeyEithBigInt(k.toString, Right(BigInt(k.toInt)))
  }

  implicit object CaseClassLikeEithBool extends CaseClassLike[Either[String, Boolean], KeyEithBool] {
    def apply(k: Char): KeyEithBool = KeyEithBool(k.toString, Right(altTF(k)))
  }

  implicit object CaseClassLikeEithDouble extends CaseClassLike[Either[String, Double], KeyEithDouble] {
    def apply(k: Char): KeyEithDouble = KeyEithDouble(k.toString, Right(exDouble(k)))
  }
  implicit object CaseClassLikeEithFloat extends CaseClassLike[Either[String, Float], KeyEithFloat] {
    def apply(k: Char): KeyEithFloat = KeyEithFloat(k.toString, Right(exFloat(k)))
  }

  implicit object CaseClassLikeEithInt extends CaseClassLike[Either[String, Int], KeyEithInt] {
    def apply(k: Char): KeyEithInt = KeyEithInt(k.toString, Right(k.toInt))
  }

  implicit object CaseClassLikeEithLong extends CaseClassLike[Either[String, Long], KeyEithLong] {
    def apply(k: Char): KeyEithLong = KeyEithLong(k.toString, Right(k.toLong))
  }

  implicit object CaseClassLikeEithStr extends CaseClassLike[Either[String, String], KeyEithStr] {
    def apply(k: Char): KeyEithStr = KeyEithStr(k.toString, Right(exStr(k)))
  }
}
