/** @see http://en.wikipedia.org/wiki/Interquartile_mean
  * @see http://en.wikipedia.org/wiki/Interquartile_range
  */
package org.gs.algebird.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import com.twitter.algebird._
import org.scalatest.{ FlatSpecLike, Matchers }
import org.gs.algebird._
import org.gs.algebird.fixtures.QTreeBuilder

/** @author garystruthers
  *
  */
class QTreeFlowSpec extends FlatSpecLike with QTreeBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  def sourceBD = TestSource.probe[QTree[BigDecimal]]
  def sourceBI = TestSource.probe[QTree[BigInt]]
  def sourceD = TestSource.probe[QTree[Double]]
  def sourceF = TestSource.probe[QTree[Float]]
  def sourceI = TestSource.probe[QTree[Int]]
  def sourceL = TestSource.probe[QTree[Long]]

  def sinkD = TestSink.probe[Double]
  def sinkDD = TestSink.probe[(Double, Double)]


  "A QTree[BigDecimal] Flow" should "return its minimum" in {
    val (pub, sub) = sourceBD
      .via(qTreeMinFlow)
      .toMat(sinkD)(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(qtBD)
    val min = sub.expectNext()
    pub.sendComplete()
    sub.expectComplete()
    assert(min <= bigDecimals.min)
  }
  
  "A QTree[BigInt] Flow" should "return its maximum" in {
    val (pub, sub) = sourceBI
      .via(qTreeMaxFlow)
      .toMat(sinkD)(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(qtBI)
    val max = sub.expectNext()
    pub.sendComplete()
    sub.expectComplete()
    assert(max >= bigInts.max.toDouble)
  }
  
  "A QTree[Double] Flow" should "return its 1st quartile bounds" in {
    val (pub, sub) = sourceD
      .via(firstQuartileFlow)
      .toMat(sinkDD)(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(qtD)
    val qB = sub.expectNext()
    pub.sendComplete()
    sub.expectComplete()
    assert(qB._1 >= 110)
    assert(qB._2 <= 110 + 0.0001)
  }
  
  "A QTree[Float] Flow" should "return its second quartile bounds" in {
    val (pub, sub) = sourceF
      .via(secondQuartileFlow)
      .toMat(sinkDD)(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(qtF)
    val qB = sub.expectNext()
    pub.sendComplete()
    sub.expectComplete()
    assert(qB._1 >= 119)
    assert(qB._2 <= 119 + 0.9001)
  }
  
  "A QTree[Int] Flow" should "return its third quartile bounds" in {
    val (pub, sub) = sourceI
      .via(thirdQuartileFlow)
      .toMat(sinkDD)(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(qtI)
    val qB = sub.expectNext()
    pub.sendComplete()
    sub.expectComplete()
    assert(qB._1 >= 115.0)
    assert(qB._2 <= 115.0 + 0.0001)
  }
  
  "A QTree[Long] Flow" should "return its inter quartile mean" in {
    val (pub, sub) = sourceL
      .via(interQuartileMeanLFlow)
      .toMat(sinkDD)(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(qtL)
    val iqm = sub.expectNext()
    pub.sendComplete()
    sub.expectComplete()
    assert(iqm._1 > 100.0)
    assert(iqm._2 < 117.93)
  }
}
