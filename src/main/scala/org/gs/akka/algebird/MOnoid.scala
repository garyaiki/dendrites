/**
  */
package org.gs.akka.algebird

import examples._



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

  val kv = KeyValue("a", 1)

  def sum[A](xs: List[A])(implicit ev: Monoid[A]): A = ev.sum(xs)
  val kvl = List(KeyValue("a", 1), KeyValue("b", 2), KeyValue("c", 3))
  sum[KeyValue](kvl)(KeyValueMonoid)
  sum(List(1,2,3))
  implicit object ListFunctor extends Functor[List] {
    def map[A, B](fa: List[A])(f: A => B): List[B] = (for (a <- fa) yield f(a))
  }

  import scala.language.higherKinds
  def aToString[A](ev: Monoid[A])(a: A): String = if(a != ev.zero) a.toString else ""
  val cA2S = aToString[KeyValue](KeyValueMonoid)_
  val lf = ListFunctor.map(kvl)(cA2S)
  val lf2 = ListFunctor.map(kvl)((a: KeyValue) => KeyValue(a.k, a.v + 5))
  def isZero[A](x: A)(implicit ev: Monoid[A]): Boolean = x == ev.zero
  def isNotZero[A](x: A)(implicit ev: Monoid[A]): Boolean = x != ev.zero
  def monoidFilter[A](x: A, f: A => Boolean): Boolean = f(x)
  val ov = OrVal
  val ovm = ov.monoid
  OrVal(true)
  ovm.plus(OrVal(KeyValueMonoid.isNonZero(KeyValue("a", 1))), OrVal(KeyValueMonoid.isNonZero(KeyValue("a", 1))))
  AndVal.monoid.plus(AndVal(KeyValueMonoid.isNonZero(KeyValue("a", 1))), AndVal(KeyValueMonoid.isNonZero(KeyValue("a", 1))))
  OrVal(KeyValueMonoid.isNonZero(KeyValue("a", 1)))
  AndVal(KeyValueMonoid.isNonZero(KeyValue("a", 1)))
  val bi = BigInt(10)
  val mKVit = Monoid.intTimes[KeyValue](bi, KeyValue("a", 1))(KeyValueMonoid)






}