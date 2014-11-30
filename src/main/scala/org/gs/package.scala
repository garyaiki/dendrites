/** @see http://www.scala-lang.org/api/current/index.html#scala.Product
  */
package org

/** @author Gary Struthers
  *
  */
package object gs {

  /** Extract a list of a specified single element from a list of case classes or tuples
    *
    * Product is the base trait for all case classes and tuples
    * @tparam A type of element
    * @param l list of case classes or tuples
    * @param element zero based index of element tuple ._2 == 1, first element of case class == 0
    * @return list of just that element, but of type Any
    * @throws ClassCastException if element doesn't match type param
    */

  def extractElement[A](l: List[Product], element: Int) =
    for (p <- l) yield p.productElement(element).asInstanceOf[A]
}