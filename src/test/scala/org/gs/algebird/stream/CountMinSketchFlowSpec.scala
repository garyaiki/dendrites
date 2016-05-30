/**
  */
package org.gs.algebird.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Keep }
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import com.twitter.algebird._
import com.twitter.algebird.CMSHasherImplicits._
import java.net.InetAddress
import language.postfixOps
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import scala.collection.immutable.Range
import util.Random
import org.gs.algebird.{createCMSMonoid, createCountMinSketch, sumCountMinSketch}
import org.gs.fixtures.InetAddressesBuilder

/** @author Gary Struthers
  *
  */
class CountMinSketchFlowSpec extends FlatSpecLike with InetAddressesBuilder {
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

  "A CountMinSketch" should "estimate number of elements seen so far" in {
    assert(streamedLongs.size === cms.totalCount)
  }
  val rnd = new Random(1)

  it should "estimate frequency of values" in {
    val addrs = inetAddresses(ipRange)
    val longZips = inetToLongZip(addrs)
    for (i <- 0 until 10) {
      val j = ipRange(rnd.nextInt(ipRange length))
      val longAddr = longZips(j)
      assert(cms.frequency(longAddr._1).estimate === j)
    }
  }

  it should "sum total count over a Sequence of them" in {
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
    assert(cms2.totalCount === (cms.totalCount * 2))
  }
}
