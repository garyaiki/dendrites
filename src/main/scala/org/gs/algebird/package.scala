/**
  */
package org.gs

import com.twitter.algebird.{ Field, Group, Monoid, NumericRing, Ring, Semigroup }

/** @author garystruthers
  *
  */
package object algebird {

  /** Sums list elements
    * @tparam A element type that can be added
    * @param xs list
    * @param ev Semigroup for type A
    * @return Option of sum or None for empty list
    */
  def sumOption[A](xs: List[A])(implicit ev: Semigroup[A]): Option[A] = ev.sumOption(xs)

  /** Sums list elements
    * @tparam A element type that can be added and has a zero identity
    * @param xs list
    * @param ev Monoid for type A
    * @return sum or Monoid[A] zero for empty list
    */
  def sum[A](xs: List[A])(implicit ev: Monoid[A]): A = ev.sum(xs)

  /** Negates an element
    * @tparam A element type that can be negated
    * @param x element to negate
    * @param ev Group for type A
    * @return -x
    */
  def negate[A](x: A)(implicit ev: Group[A]): A = ev.negate(x)

  /** Subtracts an element from another
    * @tparam A element type that can be negated
    * @param l
    * @param r
    * @param ev Group for type A
    * @return l - r
    */
  def minus[A](l: A, r: A)(implicit ev: Group[A]): A = ev.minus(l, r)

  /** Multiplies an element by another
    * @tparam A element type that can be multiplied
    * @param l
    * @param r
    * @param ev Ring for type A
    * @return l * r
    */
  def times[A](l: A, r: A)(implicit ev: Ring[A]): A = ev.times(l, r)

  /** Multiplies list elements
    * @tparam A element type that can be multiplied and has a one identity
    * @param xs list
    * @param ev Ring for type A
    * @return sum or Monoid[A] zero for empty list
    */
  def product[A](xs: List[A])(implicit ev: Ring[A]): A = ev.product(xs)

  /** Reciprocal of an element
    * @tparam A element type that can divide 1 and return an A
    * @param x element to divide one
    * @param ev Field for type A
    * @return 1/x
    */
  def inverse[A](x: A)(implicit ev: Field[A]): A = ev.inverse(x)

  implicit object BigDecimalField extends NumericRing[BigDecimal] with Field[BigDecimal] {
    override def inverse(v: BigDecimal): BigDecimal = 1/ v
  }


}