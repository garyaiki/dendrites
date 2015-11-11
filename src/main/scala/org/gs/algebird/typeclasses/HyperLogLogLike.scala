/**
  */
package org.gs.algebird.typeclasses

import annotation.implicitNotFound
import com.twitter.algebird.{HyperLogLog, HyperLogLogAggregator, HLL}
import org.gs.algebird._

/** Create an HLL from Seq[Int] or Seq[Long]
  *
  *
  * @author garystruthers
  *
  */
@implicitNotFound(msg = "HyperLogLogLike type class for ${A} must be convertable to Int or Long")
trait HyperLogLogLike[A] {
  def apply(xs: Seq[A])(implicit agg: HyperLogLogAggregator): HLL
}

object HyperLogLogLike {
  
  implicit object HLLBigDecimal extends HyperLogLogLike[BigDecimal] {
    def apply(xs: Seq[BigDecimal])(implicit agg: HyperLogLogAggregator): HLL = {
      agg(xs.map(_.toLong).map(HyperLogLog.long2Bytes(_)))
    }
  }
  
  implicit object HLLBigInt extends HyperLogLogLike[BigInt] {
    def apply(xs: Seq[BigInt])(implicit agg: HyperLogLogAggregator): HLL = {
      agg(xs.map(_.toLong).map(HyperLogLog.long2Bytes(_)))
    }
  }
  
  implicit object HLLDouble extends HyperLogLogLike[Double] {
    def apply(xs: Seq[Double])(implicit agg: HyperLogLogAggregator): HLL = {
      agg(xs.map(_.toLong).map(HyperLogLog.long2Bytes(_)))
    }
  }  

  implicit object HLLFloat extends HyperLogLogLike[Float] {
    def apply(xs: Seq[Float])(implicit agg: HyperLogLogAggregator): HLL = {
      agg(xs.map(_.toLong).map(HyperLogLog.long2Bytes(_)))
    }
  }
  
  implicit object HLLInt extends HyperLogLogLike[Int] {
    def apply(xs: Seq[Int])(implicit agg: HyperLogLogAggregator): HLL = 
      agg(xs.map(HyperLogLog.int2Bytes(_)))
  }
  
  implicit object HLLLong extends HyperLogLogLike[Long] {
    def apply(xs: Seq[Long])(implicit agg: HyperLogLogAggregator): HLL = 
      agg(xs.map(HyperLogLog.long2Bytes(_)))
  }
}
