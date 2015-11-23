/**
  */
package org.gs

import scala.reflect.{ClassTag, classTag}

/** @author garystruthers
  *
  */
package object filters {

  type TypeFilter = Any => Boolean

  /** Return only the fields of a case class or tuple that match a predicate
    *
    * Can be used to filter by type
    * @param e case class or tuple
    * @param f filter or predicate function
    * @return matching elements
    */
  def productFilter[P <: Product](e: P, f: TypeFilter): IndexedSeq[Any] = {
    val iter = e.productIterator
    iter.filter(f).toIndexedSeq
  }

  type ProductFilter[P <: Product] = (P, TypeFilter) => IndexedSeq[Any]

  /** Returns only matching elements of an indexed seq of mixed case class or tuple types
    *
    *
    * @param xs Indexed Sequence of heterogeneous types of case classes or tuples
    * @param pf function returns only elements of case class or tuple matching predicate
    * @param f predicate or filter function(common types below)
    * @return IndexedSeq of matching elements
    */
  def filterProducts[P <: Product](xs: Seq[P], pf: ProductFilter[P], f: TypeFilter): Seq[Any] = {
    val l = for {
      e <- xs
      ef <- pf(e, f)
    } yield ef
    l
  }

  def isType[A: ClassTag](e: Any): Boolean = {
    evidence$1.unapply(e) match {
      case Some(x) => true
      case None => false
    }
  }

  def isOptionType[A: ClassTag](e: Any): Boolean = e match {
    case Some(x) => {
      evidence$2.unapply(e) match {
        case Some(x) => true
        case None => false
      }
    }
    case _ => false
  }
  def isEitherStringRight[A: ClassTag](e: Any): Boolean = e match {
    case Right(x) => {
      val clazz = implicitly[ClassTag[A]].runtimeClass
      clazz.isInstance(x)
    }
    case Left(x)  => x.isInstanceOf[String]
    case _        => false
  }

  /** Accept Either Left
    * @tparam A type of Left element
    * @tparam B type of Right element
    * @param in Either
    * @return true if Left
    */
  def isLeft[A, B](in: Either[A, B]): Boolean = in match {
    case Left(l) => true
    case _       => false
  }

  /** Accept Either Right
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
    * @tparam A type of Left element
    * @tparam B type of Right element
    * @param in Either
    * @return value in Right
    */
  def extractRight[A, B](in: Either[A, B]): Option[B] = in match {
    case Right(r) => Some(r)
    case _ => None
  }

  /** Extract a specified single element from a sequence of case classes or tuples
    *
    * Use when a Sequence contains different tuples or case classes and the element wanted is at the
    * same index in all of them.
    * Product is the base trait for all case classes and tuples, its productElement gets by index
    * @tparam A type of element
    * @param l sequence of case classes or tuples
    * @param element zero based index of element tuple ._2 == 1, first element of case class == 0
    * @return sequence of just that element
    * @throws ClassCastException if element doesn't match type param
    */
  def extractElementByIndex[A](l: Seq[Product], element: Int): Seq[A] =
    for {p <- l} yield p.productElement(element).asInstanceOf[A]

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
    for {p <- l} yield {
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

}

