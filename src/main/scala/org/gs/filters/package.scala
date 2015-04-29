/**
  */
package org.gs

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
  def productFilter[P <: Product](e: P, f: TypeFilter) = {
//    println(s"productFilter e:$e")
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
//    println(s"filterProducts l:${l}")
    l
  }

  def isType[A](e: Any): Boolean = e.isInstanceOf[A]
  def isBigDecimal(e: Any): Boolean = e.isInstanceOf[BigDecimal]
  def isBigInt(e: Any): Boolean = e.isInstanceOf[BigInt]
  def isBoolean(e: Any) = e.isInstanceOf[Boolean]
  def isDouble(e: Any): Boolean = e.isInstanceOf[Double]
  def isFloat(e: Any): Boolean = e.isInstanceOf[Float]
  def isInt(e: Any): Boolean = e.isInstanceOf[Int]
  def isLong(e: Any): Boolean = e.isInstanceOf[Long]
  def isString(e: Any): Boolean = e.isInstanceOf[String]
  def isOptionBigDecimal(e: Any): Boolean = e match {
    case Some(x) => x.isInstanceOf[BigDecimal]
    case _       => false
  }
  def isOptionBigInt(e: Any): Boolean = e match {
    case Some(x) => x.isInstanceOf[BigInt]
    case _       => false
  }
  def isOptionBoolean(e: Any): Boolean = e match {
    case Some(x) => x.isInstanceOf[Boolean]
    case _       => false
  }
  def isOptionDouble(e: Any): Boolean = e match {
    case Some(x) => x.isInstanceOf[Double]
    case _       => false
  }
  def isOptionFloat(e: Any): Boolean = e match {
    case Some(x) => x.isInstanceOf[Float]
    case _       => false
  }
  def isOptionInt(e: Any): Boolean = e match {
    case Some(x) => x.isInstanceOf[Int]
    case _       => false
  }
  def isOptionLong(e: Any): Boolean = e match {
    case Some(x) => x.isInstanceOf[Long]
    case _       => false
  }
  def isOptionString(e: Any): Boolean = e match {
    case Some(x) => x.isInstanceOf[String]
    case _       => false
  }
  def isEitherStringBigDecimal(e: Any): Boolean = e match {
    case Right(x) => x.isInstanceOf[BigDecimal]
    case Left(x)  => x.isInstanceOf[String]
    case _        => false
  }
  def isEitherStringBigInt(e: Any): Boolean = e match {
    case Right(x) => x.isInstanceOf[BigInt]
    case Left(x)  => x.isInstanceOf[String]
    case _        => false
  }
  def isEitherStringBoolean(e: Any): Boolean = e match {
    case Right(x) => x.isInstanceOf[Boolean]
    case Left(x)  => x.isInstanceOf[String]
    case _        => false
  }
  def isEitherStringDouble(e: Any): Boolean = e match {
    case Right(x) => x.isInstanceOf[Double]
    case Left(x)  => x.isInstanceOf[String]
    case _        => false
  }
  def isEitherStringFloat(e: Any): Boolean = e match {
    case Right(x) => x.isInstanceOf[Float]
    case Left(x)  => x.isInstanceOf[String]
    case _        => false
  }
  def isEitherStringInt(e: Any): Boolean = e match {
    case Right(x) => x.isInstanceOf[Int]
    case Left(x)  => x.isInstanceOf[String]
    case _        => false
  }
  def isEitherStringLong(e: Any): Boolean = e match {
    case Right(x) => x.isInstanceOf[Long]
    case Left(x)  => x.isInstanceOf[String]
    case _        => false
  }
  def isEitherStringString(e: Any): Boolean = e match {
    case Right(x) => x.isInstanceOf[String]
    case Left(x)  => x.isInstanceOf[String]
    case _        => false
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
    for (p <- l) yield p.productElement(element).asInstanceOf[A]

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
}

