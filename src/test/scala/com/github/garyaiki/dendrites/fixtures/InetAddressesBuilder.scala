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
package com.github.garyaiki.dendrites.fixtures

import java.net.{Inet4Address, InetAddress}
import org.scalatest.{Outcome, TestSuite, TestSuiteMixin}

/**
  *
  * @author Gary Struthers
  */
trait InetAddressesBuilder extends TestSuiteMixin { this: TestSuite =>

  abstract override def withFixture(test: NoArgTest): Outcome = super.withFixture(test)

  /**
    * @see https://github.com/twitter...util-core...NetUtil.scala
    * @param inetAddress
    * @return
    */
  def inetAddressToLong(inetAddress: InetAddress): Long = {
    inetAddress match {
      case inetAddress: Inet4Address =>
        val addr = inetAddress.getAddress
        ((addr(0) & 0xff) << 24) | ((addr(1) & 0xff) << 16) | ((addr(2) & 0xff) << 8) | (addr(3) & 0xff)
      case _ => throw new IllegalArgumentException("non-Inet4Address cannot be converted to a Long")
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
