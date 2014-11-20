/**
  */
package org.gs.akka.algebird

import com.twitter.algebird._

/** @author garystruthers
  *
  */
object MOnoid {

  val om = new OptionMonoid[Int]()
  val plus = om plus (Some(2), Some(3))
  val sumOption = om sumOption (List(Some(1), Some(2), Some(3), Some(4)))

  val stringMonoidPlus = StringMonoid plus ("this", "is")
  val stringMonoidSumOption = StringMonoid sumOption (List("this", "is"))

  val lm = new ListMonoid[Int]()
  val lmZero = lm.zero
  val lmPlus = lm plus (List(1, 2, 3, 4), List(5, 6, 7, 8))
  val listSumOption = lm sumOption (List(lmPlus))

  val fm = new Function1Monoid[Int]()
  val fmPlus = fm plus (x => x + 1, y => y * y)
  val funComp = fmPlus(2)
  
  val orVal = OrValMonoid plus(OrVal(true), OrVal(false))
  val andVal = AndValMonoid plus(AndVal(true), AndVal(false))
  
  val monNotZero = Monoid assertNotZero[Int](0) // throws java.lang.IllegalArgumentException: argument should not be zero
  
  val monNonZero = Monoid isNonZero[Int](0)
  val monNonZeroStr = Monoid isNonZero[String]("")
  val monSum = Monoid sum[Int](List(1,2,3,4))
  val monNonZeroOption = Monoid nonZeroOption[Int](0)
  def associativeFn(x: Int, y: Int) = x * y
  def toZ(): Int = 0
  val nm = Monoid.from(toZ)(associativeFn)
  val z = nm.zero

  import examples.{KeyValue, KeyValueMonoid}
  val kv = KeyValue("a", 1)
  def sum[A](xs: List[A])(implicit ev: Monoid[A]): A = ev.sum(xs)
  sum(List(KeyValue("a", 1), KeyValue("b", 2), KeyValue("c", 3)))
  sum(List(1,2,3))
  def sumOption[A](xs: List[A])(implicit ev: Monoid[A]): Option[A] = ev.sumOption(xs)
  sumOption(List(KeyValue("a", 1), KeyValue("b", 2), KeyValue("c", 3)))
  sumOption(List[Option[KeyValue]](Some(KeyValue("a", 1)), Some(KeyValue("b", 2)), Some(KeyValue("c", 3))))
  val ov = OrVal
  val ovm = ov.monoid
  OrVal(true)
  ovm.plus(OrVal(KeyValueMonoid.isNonZero(KeyValue("a", 1))), OrVal(KeyValueMonoid.isNonZero(KeyValue("a", 1))))
  AndVal.monoid.plus(AndVal(KeyValueMonoid.isNonZero(KeyValue("a", 1))), AndVal(KeyValueMonoid.isNonZero(KeyValue("a", 1))))
  OrVal(KeyValueMonoid.isNonZero(KeyValue("a", 1)))
  AndVal(KeyValueMonoid.isNonZero(KeyValue("a", 1)))
  val bi = BigInt(10)
  val mKVit = Monoid.intTimes[KeyValue](bi, KeyValue("a", 1))






}