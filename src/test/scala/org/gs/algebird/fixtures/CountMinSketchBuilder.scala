/**
  */
package org.gs.algebird.fixtures

import org.gs._
import org.gs.fixtures.SysProcessUtils
import org.gs.algebird._
import language.postfixOps
import util.Random
import org.scalatest._
import com.twitter.algebird._

/** @author garystruthers
  *
  */
trait CountMinSketchBuilder extends SuiteMixin { this: Suite =>

  abstract override def withFixture(test: NoArgTest) = {
    super.withFixture(test)
  }

  import java.net.{ Inet4Address, InetAddress }


  /** @see https://github.com/twitter...util-core...NetUtil.scala
    * @param inetAddress
    * @return
    */
  def inetAddressToLong(inetAddress: InetAddress): Long = {
    inetAddress match {
      case inetAddress: Inet4Address =>
        val addr = inetAddress.getAddress
        ((addr(0) & 0xff) << 24) |
          ((addr(1) & 0xff) << 16) |
          ((addr(2) & 0xff) << 8) |
          (addr(3) & 0xff)
      case _ =>
        throw new IllegalArgumentException("non-Inet4Address cannot be converted to a Long")
    }
  }
  
  val ipRange = 1 to 255
  
  def inetAddresses(ipRange: Range): IndexedSeq[InetAddress] = for {
      i <- ipRange
    } yield InetAddress.getByAddress(Array[Byte](i.toByte, i.toByte, i.toByte, i.toByte))

  def inetToLongZip(addrs: IndexedSeq[InetAddress]): IndexedSeq[(Long, Int)] = {
    val longs = addrs.map(inetAddressToLong)
    longs.zipWithIndex
  }

  def testLongs(zipLongs: IndexedSeq[(Long, Int)]): IndexedSeq[Long] = {

    def f(si: (Long, Int)): IndexedSeq[Long] = IndexedSeq.fill(si._2)(si._1)

    for {
      i <- zipLongs
      out <- f(i)
    } yield out
  }
}