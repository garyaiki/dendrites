/**
  */
package com.github.garyaiki.dendrites.algebird

import language.postfixOps
import util.Random
import com.twitter.algebird._
import com.twitter.algebird.CMSHasherImplicits._
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import com.github.garyaiki.dendrites._
import com.github.garyaiki.dendrites.algebird._
import com.github.garyaiki.dendrites.fixtures.InetAddressesBuilder

/** @author garystruthers
  *
  */
class CountMinSketchSpec extends FlatSpecLike with InetAddressesBuilder {

  val addrs = inetAddresses(ipRange)
  val longZips = inetToLongZip(addrs)
  val longs = testLongs(longZips)

  implicit val m = createCMSMonoid[Long]()
  val cms0 = createCountMinSketch(longs)
  val cms1 = createCountMinSketch(longs)
  val cmss = Vector(cms0, cms1)

  "A CountMinSketch" should "estimate number of elements seen so far" in {
    assert(longs.size === cms0.totalCount)
  }

  val rnd = new Random(1)

  it should "estimate frequency of values" in {
    for (i <- 0 until 10) {
      val j = ipRange(rnd.nextInt(ipRange length))
      val longAddr = longZips(j)
      assert(cms0.frequency(longAddr._1).estimate === j)
    }
  }

  it should "sum total count over a Sequence of them" in {
    val cms = sumCountMinSketch(cmss)
    assert(cms.totalCount === (cms0.totalCount + cms1.totalCount))
  }

  it should "sum estimate frequency of values over a Sequence of them" in {
    for (i <- 0 until 10) {
      val j = ipRange(rnd.nextInt(ipRange length))
      val longAddr = longZips(j)
      val cms = sumCountMinSketch(cmss)
      assert(cms.frequency(longAddr._1).estimate === (j * 2))
    }
  }
}
