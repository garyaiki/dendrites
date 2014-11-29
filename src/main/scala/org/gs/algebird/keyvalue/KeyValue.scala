/**
  */
package org.gs.algebird.keyvalue

import com.twitter.algebird.Semigroup

/** @author garystruthers
  *
  */
object TypedKeyValue {
  //sealed abstract class KV

  case class KeyValue[A, B](k: A, v: B)
  def extractValues[A, B](l: List[KeyValue[A, B]]) = for {
    kv <- l
    kvu <- KeyValue.unapply(kv)
  } yield {
    kvu._2
  }

  val kvBD = List(KeyValue("a", BigDecimal(1)), KeyValue("b", BigDecimal(2)),
    KeyValue("c", BigDecimal(3)), KeyValue("d", BigDecimal(4)))
  val kvD = List(KeyValue("a", 1.0), KeyValue("b", 2.0), KeyValue("c", 3.0), KeyValue("d", 4.0))
  val kvI = List(KeyValue("a", 1), KeyValue("b", 2), KeyValue("c", 3), KeyValue("d", 4))
  val kvL = List(KeyValue("a", 1L), KeyValue("b", 2L), KeyValue("c", 3L), KeyValue("d", 4L))
  val kvS = List(KeyValue("a", "1"), KeyValue("b", "2"), KeyValue("c", "3"), KeyValue("d", "4"))

  val a = extractValues(kvBD)
  val b = extractValues(kvD)
  val c = extractValues(kvI)
  val d = extractValues(kvL)
  val e = extractValues(kvS)

  type A = String
  type B = Int
  type C = KeyValue[A, B]

  implicit object KeyValueSemigroup extends Semigroup[C] {
    def plus(a: C, b: C) = KeyValue(a.k, a.v + b.v)
  }

  val kvbdu = KeyValue.unapply(KeyValue("a", BigDecimal(1)))
  val kvbdus = for (kv <- kvBD) yield {
    KeyValue.unapply(kv)
  }

  val kvbdus2 = for {
    kv <- kvBD
    kvu <- KeyValue.unapply(kv)
  } yield {
    kvu._2
  }
  val flatkvbd = kvbdus.flatten

}