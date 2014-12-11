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
}