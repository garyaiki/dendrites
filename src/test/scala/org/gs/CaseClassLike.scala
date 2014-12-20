/**
  */
package org.gs

case class KeyBigDecimal(k: String, v: BigDecimal)
case class KeyBigInt(k: String, v: BigInt)
case class KeyBoolean(k: String, v: Boolean)
case class KeyDouble(k: String, v: Double)
case class KeyFloat(k: String, v: Float)
case class KeyInt(k: String, v: Int)
case class KeyLong(k: String, v: Long)
case class KeyString(k: String, v: String)

import annotation.implicitNotFound
@implicitNotFound(msg = "Cannot find CaseClassLike type class for ${A}")
trait CaseClassLike[A, B] {
  def apply(k: Char): B
}
/** @author garystruthers
  *
  */
object CaseClassLike {
  implicit object CaseClassLikeBigDecimal extends CaseClassLike[BigDecimal, KeyBigDecimal] {
    def apply(k: Char): KeyBigDecimal = KeyBigDecimal(k.toString, k.toInt)
  }
  implicit object CaseClassLikeBigInt extends CaseClassLike[BigInt, KeyBigInt] {
    def apply(k: Char): KeyBigInt = KeyBigInt(k.toString, k.toInt)
  }
  implicit object CaseClassLikeBoolean extends CaseClassLike[Boolean, KeyBoolean] {
    def apply(k: Char): KeyBoolean = KeyBoolean(k.toString, if(k % 2 == 0) true else false)
  }
  implicit object CaseClassLikeDouble extends CaseClassLike[Double, KeyDouble] {
    def apply(k: Char): KeyDouble = KeyDouble(k.toString, k.toInt * 1.1)
  }
  implicit object CaseClassLikeFloat extends CaseClassLike[Float, KeyFloat] {
    def apply(k: Char): KeyFloat = KeyFloat(k.toString, k.toInt * 1.1f)
  }
  implicit object CaseClassLikeInt extends CaseClassLike[Int, KeyInt] {
    def apply(k: Char): KeyInt = KeyInt(k.toString, k.toInt)
  }
  implicit object CaseClassLikeLong extends CaseClassLike[Long, KeyLong] {
    def apply(k: Char): KeyLong = KeyLong(k.toString, k.toInt)
  }
  implicit object CaseClassLikeString extends CaseClassLike[String, KeyString] {
    def apply(k: Char): KeyString = KeyString(k.toString, k.toString.toUpperCase())
  }
}