/** @see https://github.com/twitter/algebird/...QTree.scala
  * @see http://www.snip2code.com/Snippet/67332/Calculating-the-median-distance-and-time
  */
package org.gs.akka.algebird

import com.twitter.algebird._

/** @author garystruthers
  *
  */
object Qtree {

  val list = (1L to 1000).map { i => math.random }
  val k = 6
  val qtSemigroup = new QTreeSemigroup[Double](k)
  qtSemigroup.plus(QTree(5.0), QTree(6.0))
  val buildQTree = list.map { QTree(_) }.reduce { qtSemigroup.plus(_, _) }
  val quantile = math.random

  val (lower, upper) = buildQTree.quantileBounds(quantile)
  val count = buildQTree.count
  val rangeSumBounds = buildQTree.rangeSumBounds(lower, upper)
  val rangeCountBounds = buildQTree.rangeCountBounds(lower, upper)
  val compress = buildQTree.compress(1)

}