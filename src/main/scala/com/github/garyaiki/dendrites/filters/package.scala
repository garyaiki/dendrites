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
package com.github.garyaiki.dendrites

import scala.reflect.ClassTag

/** Filter and extractor functions for sequences, Either, Option, tuples and case classes
  *
  * Filter Sequence of different types of case classes
  * {{{
  * val mixCaseClasses = keyBigDecimal ++ keyBigInt ++ keyBoolean ++ keyDouble ++ keyFloat
  * val filtered = filterProducts(mixCaseClasses, fieldFilter, isType[BigDecimal])
  * }}}
  * Filter by type
  * {{{
  * if(filtered.forall(isType[BigDecimal]))
  * }}}
  * Filter by Option[type]
  * {{{
  * val filtered = filterProducts(mixCaseClasses, fieldFilter, isOptionType[Int])
  * }}}
  * Filter by Either where Left is a String or Right is the type
  * {{{
  * val filtered = filterProducts(mixCaseClasses, fieldFilter, isEitherStringRight[BigInt])
  * }}}
  * Extract a case class field by index
  * {{{
  * val eithBigDecs = extractElementByIndex[Either[String, BigDecimal]](keyEithBigDec, 1)
  * }}}
  * Filter Right values of Either
  * {{{
  * def collectRightFlow[A, B]: Flow[Seq[Either[A, B]], Seq[B], NotUsed] =
  *       Flow[Seq[Either[A, B]]].collect(PartialFunction(filterRight))
  * }}}
  *  @author Gary Struthers
  */
package object filters {

  type TypeFilter = Any => Boolean

  /** Return only fields of case class or tuple that match predicate. Can filter by type
    *
    * @tparam P <: Product
    * @param e case class or tuple
    * @param f filter or predicate function
    * @return matching elements
    */
  def fieldFilter[P <: Product](e: P, f: TypeFilter): IndexedSeq[Any] = {
    val iter = e.productIterator
    iter.filter(f).toIndexedSeq
  }

  type ProductFilter[P <: Product] = (P, TypeFilter) => IndexedSeq[Any]

  /** Returns matching elements of indexed seq of mixed case class or tuple types
    *
    * @param xs Indexed Sequence of heterogeneous types of case classes or tuples
    * @param pf function returns only elements of case class or tuple matching predicate
    * @param f predicate or filter function(common types below)
    * @return IndexedSeq of matching elements
    */
  def filterProducts[P <: Product](xs: Seq[P], pf: ProductFilter[P], f: TypeFilter): Seq[Any] = {
    for {
      e <- xs
      ef <- pf(e, f)
    } yield ef
  }

  /** Filter by type
    *
    * @tparam A: ClassTag type e should be
    * @param e
    * @return true if e is type A
    */
  def isType[A: ClassTag](e: Any): Boolean = {
    evidence$1.unapply(e) match {
      case Some(x) => true
      case None => false
    }
  }

  /** Filter by type within Option
    *
    * @tparam A type of Some
    * @param e
    * @return true if e is Some(x) type A
    */
  def isOptionType[A: ClassTag](e: Any): Boolean = e match {
    case Some(x) => isType(x)
    case _ => false
  }

  /** true if Either Left is a String OR if Right is type A
    *
    * @tparam A type of Right element
    * @param e Either Left or Right
    * @return true if Left is a String OR if Right is type A
    */
  def isEitherStringRight[A: ClassTag](e: Any): Boolean = e match {
    case Right(x) => isType(x)
    case Left(x)  => x.isInstanceOf[String]
    case _        => false
  }

  /** true if Either is Left
    *
    * @tparam A type of Left element
    * @tparam B type of Right element
    * @param in Either
    * @return true if Left
    */
  def isLeft[A, B](in: Either[A, B]): Boolean = in match {
    case Left(l) => true
    case _       => false
  }

  /** true if Either is Right
    *
    * @tparam A type of Left element
    * @tparam B type of Right element
    * @param in Either
    * @return true if Right
    */
  def isRight[A, B](in: Either[A, B]): Boolean = in match {
    case Right(r) => true
    case _        => false
  }

  /** Extract value from Either Left
    *
    * @tparam A type of Left element
    * @tparam B type of Right element
    * @param in Either
    * @return optional value in Left
    */
  def extractLeft[A, B](in: Either[A, B]): Option[A] = in match {
    case Left(l) => Some(l)
    case _ => None
  }

  /** Extract value from Either Right
    *
    * @tparam A Left element
    * @tparam B Right element
    * @param in Either
    * @return value in Right
    */
  def extractRight[A, B](in: Either[A, B]): Option[B] = in match {
    case Right(r) => Some(r)
    case _ => None
  }

  /** Extract specified element type from sequence of case classes or tuples
    *
    * Use when a Sequence contains mixed tuples or case classes and the element wanted is at the
    * same index in all of them.
    *
    * @tparam A element
    * @param l sequence of case classes or tuples
    * @param element zero based index of element tuple ._2 == 1, first element of case class == 0
    * @return sequence of type A
    * @throws ClassCastException if element doesn't match type
    */
  def extractElementByIndex[A](l: Seq[Product], element: Int): Seq[A] =
    for(p <- l) yield p.productElement(element).asInstanceOf[A]

  /** Extract a Sequence of 2 element Tuples from a sequence of case classes or tuples
    *
    * Use when a Sequence contains different tuples or case classes and the tuple2 elements wanted
    * are at the same indexs in all of them.
    * Product is the base trait for all case classes and tuples
    * @tparam A type of element1
    * @tparam B type of element2
    * @param l sequence of case classes or tuples
    * @param element1 zero based index of element tuple
    * @param element2 zero based index of element tuple
    * @return sequence of (element1, element2)
    * @throws ClassCastException if element doesn't match type param
    */
  def extractTuple2ByIndex[A, B](l: Seq[Product], element1: Int, element2: Int): Seq[(A, B)] =
    for(p <- l) yield (p.productElement(element1).asInstanceOf[A], p.productElement(element2).asInstanceOf[B])

  /** Extract Seq of values from Either Right
    *
    * @tparam A type of Left element
    * @tparam B type of Right element
    * @param in Seq of Either
    * @return Seq of values in Right
    */
  def filterRight[A, B](in: Seq[Either[A, B]]): Seq[B] = in.collect { case Right(r) => r }
}
