/**
  */
package org.gs

/** @author garystruthers
  *
  */
package object aggregator {

  /** Find the arithmetic mean of a generic Sequence of Numeric elements
    *
    * @tparam A is a Numeric type
    * @param xs
    * @return mean of xs in Right or error message in Left
    */
  def mean[A: Numeric](xs: Seq[A]): Either[String, A] = implicitly[Numeric[A]] match {
    case num: Fractional[_] =>
      import num._;
      Right(xs.sum / fromInt(xs.size))
    case num: Integral[_] =>
      import num._;
      Right(xs.sum / fromInt(xs.size))
    case x => Left(s"$x is not divisable")
  }
}
