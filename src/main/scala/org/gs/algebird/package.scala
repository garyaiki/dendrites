/**
  */
package org.gs

import com.twitter.algebird.{ Group, Monoid, Semigroup }

/** @author garystruthers
  *
  */
package object algebird {

  /** Sums list elements
    * @tparam A element type
    * @param xs list
    * @param ev Semigroup for type A
    * @return Option of sum or None for empty list
    */
  def sumOption[A](xs: List[A])(implicit ev: Semigroup[A]): Option[A] = ev.sumOption(xs)

  /** Sums list elements
    * @tparam A element type
    * @param xs list
    * @param ev Monoid for type A
    * @return sum or Monoid[A] zero for empty list
    */
  def sum[A](xs: List[A])(implicit ev: Monoid[A]): A = ev.sum(xs)

  /** Negates an element
    * @tparam A element type
    * @param xs list
    * @param ev Group for type A
    * @return sum or Monoid[A] zero for empty list
    */
  def negate[A](x: A)(implicit ev: Group[A]): A = ev.negate(x)

  implicit object BigDecimalSemigroup extends Semigroup[BigDecimal] {
    def plus(a: BigDecimal, b: BigDecimal) = a + b
  }

  implicit object BigDecimalMonoid extends Monoid[BigDecimal] {
    def zero = BigDecimal(0)
    def plus(a: BigDecimal, b: BigDecimal) = BigDecimalSemigroup.plus(a, b)
  }

  implicit object BigDecimalGroup extends Group[BigDecimal] {
    def zero = BigDecimalMonoid.zero
    def plus(a: BigDecimal, b: BigDecimal) = BigDecimalMonoid.plus(a, b)
    override def negate(v: BigDecimal): BigDecimal = minus(zero, v) 
  }

}