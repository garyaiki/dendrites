/**

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.github.garyaiki.dendrites.algebird.typeclasses

import annotation.implicitNotFound
import com.twitter.algebird.{HyperLogLog, HyperLogLogAggregator, HLL}

/** Create an HLL from Seq[A]
  *
  * @tparam A convertable to Int or Long
  * @see [[http://danielwestheide.com/blog/2013/02/06/the-neophytes-guide-to-scala-part-12-type-classes.html type-classes]]
  * @author Gary Struthers
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
