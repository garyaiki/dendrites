/**
  */
package org.gs

/** @author garystruthers
  *
  */
package object aggregator {

  /** Return only the fields of a case class or tuple that match a predicate
    *
    * Can be used to filter by type
    * @param x case class or tuple
    * @param f filter function
    * @return matching elements
    */
  def filterType[P <: Product](e: P)(f: Any => Boolean) = {

    val iter = e.productIterator
    iter.filter(f).toIndexedSeq
  }

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
}

