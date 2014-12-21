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

case class KeyOptBigDec(k: String, v: Option[BigDecimal])
case class KeyOptBigInt(k: String, v: Option[BigInt])
case class KeyOptBool(k: String, v: Option[Boolean])
case class KeyOptDouble(k: String, v: Option[Double])
case class KeyOptFloat(k: String, v: Option[Float])
case class KeyOptInt(k: String, v: Option[Int])
case class KeyOptLong(k: String, v: Option[Long])
case class KeyOptStr(k: String, v: Option[String])

case class KeyEithBigDec(k: String, v: Either[String, BigDecimal])
case class KeyEithBigInt(k: String, v: Either[String, BigInt])
case class KeyEithBool(k: String, v: Either[String, Boolean])
case class KeyEithDouble(k: String, v: Either[String, Double])
case class KeyEithFloat(k: String, v: Either[String, Float])
case class KeyEithInt(k: String, v: Either[String, Int])
case class KeyEithLong(k: String, v: Either[String, Long])
case class KeyEithStr(k: String, v: Either[String, String])

import annotation.implicitNotFound
@implicitNotFound(msg = "Cannot find CaseClassLike type class for ${A}")
trait CaseClassLike[A, B] {
  def apply(k: Char): B
}
/** @author garystruthers
  *
  */
object CaseClassLike {
  def altTF(k: Char) = if (k % 2 == 0) (true) else (false)
  implicit val mc = new java.math.MathContext(2)
  def exDouble(k: Char)(implicit ev: java.math.MathContext) = BigDecimal(k.toInt * 1.1, ev).toDouble
  def exFloat(k: Char) = k.toInt * 1.1f
  def exStr(k: Char) = k.toString.toUpperCase()
  def exLeftStr(k: Char) = s"Error key:$k"

  implicit object CaseClassLikeBigDecimal extends CaseClassLike[BigDecimal, KeyBigDecimal] {
    def apply(k: Char): KeyBigDecimal = KeyBigDecimal(k.toString, k.toInt)
  }
  implicit object CaseClassLikeBigInt extends CaseClassLike[BigInt, KeyBigInt] {
    def apply(k: Char): KeyBigInt = KeyBigInt(k.toString, k.toInt)
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
    def apply(k: Char): KeyLong = KeyLong(k.toString, k.toInt)
  }
  implicit object CaseClassLikeString extends CaseClassLike[String, KeyString] {
    def apply(k: Char): KeyString = KeyString(k.toString, exStr(k))
  }

  implicit object CaseClassLikeOptBigDec extends CaseClassLike[Option[BigDecimal], KeyOptBigDec] {
    def apply(k: Char): KeyOptBigDec = KeyOptBigDec(k.toString, Some(k.toInt))
  }
  implicit object CaseClassLikeOptBigInt extends CaseClassLike[Option[BigInt], KeyOptBigInt] {
    def apply(k: Char): KeyOptBigInt = KeyOptBigInt(k.toString, Some(k.toInt))
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
    def apply(k: Char): KeyOptLong = KeyOptLong(k.toString, Some(k.toInt))
  }
  implicit object CaseClassLikeOptStr extends CaseClassLike[Option[String], KeyOptStr] {
    def apply(k: Char): KeyOptStr = KeyOptStr(k.toString, Some(exStr(k)))
  }

  implicit object CaseClassLikeEithBigDec extends
      CaseClassLike[Either[String, BigDecimal], KeyEithBigDec] {
    def apply(k: Char): KeyEithBigDec = KeyEithBigDec(k.toString, Right(k.toInt))
  }
  implicit object CaseClassLikeEithBigInt extends
      CaseClassLike[Either[String, BigInt], KeyEithBigInt] {
    def apply(k: Char): KeyEithBigInt = KeyEithBigInt(k.toString, Right(k.toInt))
  }
  implicit object CaseClassLikeEithBool extends
      CaseClassLike[Either[String, Boolean], KeyEithBool] {
    def apply(k: Char): KeyEithBool = KeyEithBool(k.toString, Right(altTF(k)))
  }
  implicit object CaseClassLikeEithDouble extends
      CaseClassLike[Either[String, Double], KeyEithDouble] {
    def apply(k: Char): KeyEithDouble = KeyEithDouble(k.toString, Right(exDouble(k)))
  }
  implicit object CaseClassLikeEithFloat extends
      CaseClassLike[Either[String, Float], KeyEithFloat] {
    def apply(k: Char): KeyEithFloat = KeyEithFloat(k.toString, Right(exFloat(k)))
  }
  implicit object CaseClassLikeEithInt extends
      CaseClassLike[Either[String, Int], KeyEithInt] {
    def apply(k: Char): KeyEithInt = KeyEithInt(k.toString, Right(k.toInt))
  }
  implicit object CaseClassLikeEithLong extends CaseClassLike[Either[String, Long], KeyEithLong] {
    def apply(k: Char): KeyEithLong = KeyEithLong(k.toString, Right(k.toInt))
  }
  implicit object CaseClassLikeEithStr extends CaseClassLike[Either[String, String], KeyEithStr] {
    def apply(k: Char): KeyEithStr = KeyEithStr(k.toString, Right(exStr(k)))
  }

}