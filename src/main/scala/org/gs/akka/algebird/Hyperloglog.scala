/** @see http://research.neustar.biz/2012/10/25/sketch-of-the-day-hyperloglog-cornerstone-of-a-big-data-infrastructure/
  * @see http://highscalability.com/blog/2012/4/5/big-data-counting-how-to-count-a-billion-distinct-objects-us.html
  */
package org.gs.akka.algebird

import com.twitter.algebird._

/** @author garystruthers
  *test
  */
object Hyperloglog {

  val a = HyperLogLogAggregator(12)
  val m = new HyperLogLogMonoid(12)
  import HyperLogLog._

  val hll = m(12) + m(30)
  hll.estimatedSize

  val hll2 = new HyperLogLogMonoid(4)
  val data = List(1, 1, 2, 2, 3, 3, 4, 4, 5, 5)
  val seqHll = data.map { hll2(_) }
  val sumHll = hll2.sum(seqHll)
  val approxSizeOf = hll2.sizeOf(sumHll)
  val actualSize = data.toSet.size
  val estimate = approxSizeOf.estimate

  val r = new java.util.Random
  def exactCount[T](it: Iterable[T]): Int = it.toSet.size
  List(5, 7, 10).foreach(bits => {
    val aggregator = HyperLogLogAggregator(bits)
    val data = (0 to 10000).map { i => r.nextInt(1000) }
    val exact = exactCount(data).toDouble
    val approxCount = aggregator(data.map(int2Bytes(_))).approximateSize.estimate.toDouble
  })
}