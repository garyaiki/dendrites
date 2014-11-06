/** Quickly filter out items probably not in the Bloom Filter
  *
  * @see https://github.com/twitter/algebird/...BloomFilter.scala
  */
package org.gs.akka.algebird

import com.twitter.algebird._

/** @author garystruthers
  *
  */
object Bloomfilter {

  val NUM_HASHES = 6
  val WIDTH = 32
  val SEED = 1
  val bfMonoid = new BloomFilterMonoid(NUM_HASHES, WIDTH, SEED)
  val goodItems = List[String]("1", "2", "3", "4", "100")
  val bf = bfMonoid.create(goodItems:_*)
  val approxBool = bf.contains("1")
  val res = approxBool.isTrue
  val items = List("1", "2", "3", "4","10", "20", "30", "40", "100")
  val postBloom = items.filter(bf.contains(_).isTrue) // may have false positives
}