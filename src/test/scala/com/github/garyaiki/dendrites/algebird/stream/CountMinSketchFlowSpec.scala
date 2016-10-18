/** Copyright 2016 Gary Struthers

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
package com.github.garyaiki.dendrites.algebird.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.twitter.algebird._
import com.twitter.algebird.CMSHasherImplicits._
import java.net.InetAddress
import language.postfixOps
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import scala.collection.immutable.Range
import util.Random
import com.github.garyaiki.dendrites.algebird.{createCMSMonoid, createCountMinSketch, sumCountMinSketch}
import com.github.garyaiki.dendrites.fixtures.InetAddressesBuilder

/** @author Gary Struthers
  *
  */
class CountMinSketchFlowSpec extends WordSpecLike with InetAddressesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  val addrs: Flow[Range, IndexedSeq[InetAddress], NotUsed] = Flow[Range].map(inetAddresses)
  val longZips: Flow[IndexedSeq[InetAddress], IndexedSeq[(Long, Int)], NotUsed] =
    Flow[IndexedSeq[InetAddress]].map(inetToLongZip)
  val longs: Flow[IndexedSeq[(Long, Int)], IndexedSeq[Long], NotUsed] =
    Flow[IndexedSeq[(Long, Int)]].map(testLongs)
  val longsFlow = TestSource.probe[Range]
    .via(addrs)
    .via(longZips)
    .via(longs)
  val (pubLongs, subLongs) = longsFlow.toMat(TestSink.probe[IndexedSeq[Long]])(Keep.both).run()
  subLongs.request(1)
  pubLongs.sendNext(ipRange)
  val streamedLongs = subLongs.expectNext()
  pubLongs.sendComplete()
  subLongs.expectComplete()
  implicit val m = createCMSMonoid[Long]()
  val cmSketch: Flow[IndexedSeq[Long], CMS[Long], NotUsed] =
    Flow[IndexedSeq[Long]].map(createCountMinSketch[Long])
  val (pub, sub) = TestSource.probe[IndexedSeq[Long]].via(cmSketch)
    .toMat(TestSink.probe[CMS[Long]])(Keep.both).run()
  sub.request(1)
  pub.sendNext(streamedLongs)
  val cms = sub.expectNext()
  pub.sendComplete()
  sub.expectComplete()

  "A CountMinSketch" should {
    "estimate number of elements seen so far" in { streamedLongs.size shouldBe cms.totalCount }
  }
  val rnd = new Random(1)

  it should {
    "estimate frequency of values" in {
    	val addrs = inetAddresses(ipRange)
    			val longZips = inetToLongZip(addrs)
    			for (i <- 0 until 10) {
    				val j = ipRange(rnd.nextInt(ipRange length))
    						val longAddr = longZips(j)
    						cms.frequency(longAddr._1).estimate shouldBe j
    			}
    }
  }

  it should {
    "sum total count over a Sequence of them" in {
    	val cmss = Vector(cms, cms)
    			val cmsSummer: Flow[Seq[CMS[Long]], CMS[Long], NotUsed] =
    			Flow[Seq[CMS[Long]]].map(sumCountMinSketch[Long])
    			val (pub, sub) = TestSource.probe[Seq[CMS[Long]]].via(cmsSummer)
    			.toMat(TestSink.probe[CMS[Long]])(Keep.both).run()
    			sub.request(1)
    			pub.sendNext(cmss)
    			val cms2 = sub.expectNext()
    			pub.sendComplete()
    			sub.expectComplete()
    			cms2.totalCount === (cms.totalCount * 2)
    }
  }
}
