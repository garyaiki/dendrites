/** @see https://github.com/twitter/algebird/...QTree.scala
  * @see http://www.snip2code.com/Snippet/67332/Calculating-the-median-distance-and-time
  * @see http://www.cs.virginia.edu/~son/cs851/papers/ucsb.sensys04.pdf
  */
package org.gs.akka.algebird

import com.twitter.algebird._
import scala.language.postfixOps

/** @author garystruthers
  *
  */
object Qtree {
 
  val list = List(1, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 5, 6, 7, 8).map(_ toDouble)
  val k = 5
  val qtSemigroup = new QTreeSemigroup[Double](k)
  val buildQTree = list.map { QTree(_) }.reduce { qtSemigroup.plus(_, _) }

  val list2 = List(2, 2, 2, 2, 2, 4, 4, 4, 4, 5, 5, 5, 6, 7, 8).map(_ toDouble)
  val buildQTree2 = list2.map { QTree(_) }.reduce { qtSemigroup.plus(_, _) }
  val merged = buildQTree.merge(buildQTree2)
  val qTrees = List(buildQTree, buildQTree2)
  val reduced = qTrees reduce(_.merge(_))
  val sameRangeQTrees = qTrees filter(_.range == qTrees(0).range) 
  val sameRangeReducedQTrees = qTrees filter(_.range == qTrees(0).range) reduce(_.merge(_))
  val quantile = math.random

  val (lower, upper) = buildQTree.quantileBounds(quantile)
  val count = buildQTree.count
  val rangeSumBounds = buildQTree.rangeSumBounds(lower, upper)
  val rangeCountBounds = buildQTree.rangeCountBounds(lower, upper)
  val compress = buildQTree.compress(1)

}