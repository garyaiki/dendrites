/**
  */
package org.gs.algebird

import language.postfixOps
import util.Random
import com.twitter.algebird._
import com.twitter.algebird.CMSHasherImplicits._
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import org.gs._
import org.gs.algebird._
import org.gs.algebird.fixtures.CountMinSketchBuilder

/** @author garystruthers
  *
  */
class CountMinSketchSpec extends FlatSpecLike with CountMinSketchBuilder {

  val addrs = inetAddresses(ipRange)
  val longZips = inetToLongZip(addrs)
  val longs = testLongs(longZips)

  implicit val m = createCMSMonoid[Long]()
  implicit val cms = createCountMinSketch(longs)

  "A CountMinSketch" should "estimate distinct values" in {
    assert(longs.size === cms.totalCount)
  }

  val rnd = new Random(1)

  "A CountMinSketch" should "estimate frequency of values" in {
    for (i <- 0 until 10) {
      val j = ipRange(rnd.nextInt(ipRange length))
      val longAddr = longZips(j)
      assert(cms.frequency(longAddr._1).estimate === j)
    }
  }

  val cmsLR = appendCountMinSketch(longs)

  "An appended CountMinSketch" should "estimate distinct values" in {
    assert(cmsLR.totalCount === (cms.totalCount * 2))
  }

  "An appended CountMinSketch" should "estimate frequency of values" in {
    for (i <- 0 until 10) {
      val j = ipRange(rnd.nextInt(ipRange length))
      val longAddr = longZips(j)
      assert(cmsLR.frequency(longAddr._1).estimate === (j * 2))
    }
  }
}
