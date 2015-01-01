/**
  */
package org.gs.algebird

import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import org.gs.algebird.fixtures.CountMinSketchBuilder
import org.gs.algebird._
import org.gs._
import com.twitter.algebird._
import util.Random
import language.postfixOps

/** @author garystruthers
  *
  */
class CountMinSketchSpec extends FlatSpecLike with CountMinSketchBuilder {

  val addrs = inetAddresses(ipRange)
  val longZips = inetToLongZip(addrs)
  val longs = testLongs(longZips)

  implicit val m = createCMSMonoid()
  val cms = createCountMinSketch(longs)
  assert(longs.size === cms.totalCount)
  val rnd = new Random(1)

  "A highly skewed CountMinSketch" should "make good frequency estimates" in {
    for {
      i <- 0 until 10
    } {
      val j = ipRange(rnd.nextInt(ipRange length))
      val longAddr = longZips(j)
      assert(cms.frequency(longAddr._1).estimate === j)
    }
  }  
}