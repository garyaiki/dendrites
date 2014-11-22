/**
  */
package org.gs.akka.algebird

import com.twitter.algebird._

/** @author garystruthers
  *
  */
package object examples {
  case class KeyValue(k: String, v: Int)

  implicit object KeyValueSemigroup extends Semigroup[KeyValue] {
    def plus(a: KeyValue, b: KeyValue) = KeyValue(a.k, a.v + b.v)
  }

  implicit object KeyValueMonoid extends Monoid[KeyValue] {
    def zero = KeyValue("", 0)
    def plus(a: KeyValue, b: KeyValue) = KeyValueSemigroup.plus(a, b)
  }

  implicit object KeyValueGroup extends Group[KeyValue] {
    def zero = KeyValueMonoid.zero
    def plus(a: KeyValue, b: KeyValue) = KeyValueSemigroup.plus(a, b)
    override def negate(v: KeyValue) = minus(zero, v)
    //def minus(l: KeyValue, r: KeyValue) = plus(l, negate(r))
  }

}