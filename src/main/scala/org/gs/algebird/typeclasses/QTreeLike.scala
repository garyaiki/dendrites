/**
  */
package org.gs.algebird.typeclasses

import annotation.implicitNotFound
import com.twitter.algebird.QTree

/** Provide 1 argument QTree factory so other QTree types can be created while traversing a Sequence
  *
  * QTree provides 1 arg apply for Double and Long.
  * These sum the original types
  *
  * @tparam A convertable to Double or Long
  * @author Gary Struthers
  */
@implicitNotFound(msg = "Cannot find QTreeLike type class for ${A}")
trait QTreeLike[A] {
  def apply(x: A): QTree[A]
}

object QTreeLike {
  implicit object QTreeBigDecimal extends QTreeLike[BigDecimal] {
    def apply(a: BigDecimal): QTree[BigDecimal] = QTree.apply((a.toDouble, a))
  }

  implicit object QTreeBigInt extends QTreeLike[BigInt] {
    def apply(a: BigInt): QTree[BigInt] = QTree.apply((a.toDouble, a))
  }

  implicit object QTreeDouble extends QTreeLike[Double] {
    def apply(a: Double): QTree[Double] = QTree.apply((a -> a))
  }

  implicit object QTreeFloat extends QTreeLike[Float] {
    def apply(a: Float): QTree[Float] = QTree.apply((a.toDouble, a))
  }

  implicit object QTreeInt extends QTreeLike[Int] {
    def apply(a: Int): QTree[Int] = QTree.apply((a.toDouble, a))
  }

  implicit object QTreeLong extends QTreeLike[Long] {
    def apply(a: Long): QTree[Long] = QTree.apply((a -> a))
  }
}
