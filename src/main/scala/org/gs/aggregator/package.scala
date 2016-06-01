
package org.gs

/** aggregate functions
  *
  * Mean of sequence of Numeric
  * {{{
  * val doubles = List(1.0, 2.1, 3.2)
  * val m: Either[String,Double] = mean(doubles)
  * }}}
  * @author Gary Struthers
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
    case num: Numeric[_] => Left(s"$num is not divisable")
  }
}
