/**
  */
package org.gs.algebird.keyvalue

import com.twitter.algebird.Semigroup

/** @author garystruthers
  *
  */
object TypedKeyValue {

  case class KeyValue[A, B](k: A, v: B)

  val kvBD = List(KeyValue("a", BigDecimal(1)), KeyValue("b", BigDecimal(2)),
    KeyValue("c", BigDecimal(3)), KeyValue("d", BigDecimal(4)))
  val kvD = List(KeyValue("a", 1.0), KeyValue("b", 2.0), KeyValue("c", 3.0), KeyValue("d", 4.0))
  val kvI = List(KeyValue("a", 1), KeyValue("b", 2), KeyValue("c", 3), KeyValue("d", 4))
  val kvL = List(KeyValue("a", 1L), KeyValue("b", 2L), KeyValue("c", 3L), KeyValue("d", 4L))
  val kvS = List(KeyValue("a", "1"), KeyValue("b", "2"), KeyValue("c", "3"), KeyValue("d", "4"))

  import org.gs._
  val ap = extractElement[BigDecimal](kvBD, 1)
  val bp = extractElement[Double](kvD, 1)
  val cp = extractElement[Int](kvI, 1)
  val dp = extractElement[Long](kvL, 1)
  val ep = extractElement[String](kvS, 1)

  val tSBD = List(("a", BigDecimal(1)), ("b", BigDecimal(2)),
    ("c", BigDecimal(3)), ("d", BigDecimal(4)))
  val tSD = List(("a", 1.0), ("b", 2.0), ("c", 3.0), ("d", 4.0))
  val tSI = List(("a", 1), ("b", 2), ("c", 3), ("d", 4))
  val tSL = List(("a", 1L), ("b", 2L), ("c", 3L), ("d", 4L))
  val tSS = List(("a", "1"), ("b", "2"), ("c", "3"), ("d", "4"))

  
  val atBD = extractElement[BigDecimal](tSBD, 1)
  val btD = extractElement[Double](tSD, 1)
  val ctI = extractElement[Int](tSI, 1)
  val dtL = extractElement[Long](tSL, 1)
  val etS = extractElement[String](tSS, 1)


  def sumOption[A](xs: List[A])(implicit ev: Semigroup[A]): Option[A] = ev.sumOption(xs)
  // sumOption(at)
  val p: Product = KeyValue("b", 2.0)
}