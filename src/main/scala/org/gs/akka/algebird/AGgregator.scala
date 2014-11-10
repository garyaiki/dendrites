/**
  */
package org.gs.akka.algebird

import com.twitter.algebird._

/** @author garystruthers
  *
  */
object AGgregator {
  val a = Aggregator.fromReduce(Math.min)
  val b = Aggregator.fromSemigroup(Min.semigroup[Int])
  val c = b.andThenPresent(_.get)
  val d = Aggregator.fromMonoid(Min.monoid(1))
  val r = Ring
  val e = Aggregator.fromRing(r.intRing)
  val agg = Min.aggregator[Int]

  val dataPipe = List(1, 2, 3, 4)
  //val aggDP = dataPipe.aggregate(c)
  class OddCountAggregator extends Aggregator[Int, Int, String] {
    def prepare(v: Int) = v % 2
    def reduce(v1: Int, v2: Int) = v1 + v2
    def present(v: Int) = s"Odd count is: $v"
  }
  val oca = new OddCountAggregator
  val l = List(1,2,3,4,5,6,7,8)
  val s = l reduce(oca.reduce(_, _))
  oca.present(s)

}