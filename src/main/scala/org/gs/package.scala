/** @see http://www.scala-lang.org/api/current/index.html#scala.Product
  */
package org

/** @author Gary Struthers
  *
  */
package object gs {

  /** Extract a Sequence of a specified single element from a sequence of case classes or tuples
    *
    * Product is the base trait for all case classes and tuples
    * @tparam A type of element
    * @param l sequence of case classes or tuples
    * @param element zero based index of element tuple ._2 == 1, first element of case class == 0
    * @return sequence of just that element
    * @throws ClassCastException if element doesn't match type param
    */
  def extractElement[A](l: Seq[Product], element: Int) =
    for (p <- l) yield p.productElement(element).asInstanceOf[A]

  /** Extract a Sequence of 2 element Tuples from a sequence of case classes or tuples
    *
    * Product is the base trait for all case classes and tuples
    * @tparam A type of element1
    * @tparam B type of element2
    * @param l sequence of case classes or tuples
    * @param element1 zero based index of element tuple
    * @param element2 zero based index of element tuple
    * @return sequence of (element1, element2)
    * @throws ClassCastException if element doesn't match type param
    */
  def extractTuple2[A, B](l: Seq[Product], element1: Int, element2: Int): Seq[(A, B)] =
    for (p <- l) yield {
      (p.productElement(element1).asInstanceOf[A],
        p.productElement(element2).asInstanceOf[B])
    }

  /** Extract Seq of values from Either Right
    * @tparam A type of Left element
    * @tparam B type of Right element
    * @param in Seq of Either
    * @return Seq of values in Right
    */
  def filterRight[A, B](in: Seq[Either[A, B]]): Seq[B] = in.collect { case Right(r) => r }

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