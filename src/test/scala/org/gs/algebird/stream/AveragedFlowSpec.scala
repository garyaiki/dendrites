/**
  */
package org.gs.algebird.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Keep }
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import com.twitter.algebird._
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import org.gs.aggregator.mean
import org.gs.fixtures.TestValuesBuilder

/** @author garystruthers
  *
  */
class AveragedFlowSpec extends FlatSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  "An AveragedValue Flow of BigDecimals" should "be near its mean" in {

    val (pub, sub) = TestSource.probe[Seq[BigDecimal]]
      .via(avgFlow)
      .toMat(TestSink.probe[AveragedValue])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(bigDecimals)
    val avBD = sub.expectNext()
    pub.sendComplete()
    sub.expectComplete()
    assert(avBD.count === bigDecimals.size)
    val mBD = mean(bigDecimals)
    assert(avBD.value === (mBD.right.get.toDouble +- 0.005))
  }

  "An AveragedValue Flow of BigInts" should "be near its mean" in {

    val (pub, sub) = TestSource.probe[Seq[BigInt]]
      .via(avgFlow)
      .toMat(TestSink.probe[AveragedValue])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(bigInts)
    val avBI = sub.expectNext()
    val mBI = mean(bigInts)
    assert(avBI.count === bigInts.size)
    assert(avBI.value === (mBI.right.get.toDouble +- 0.5))
  }

  "An AveragedValue Flow of Doubles" should "be near their mean" in {

    val (pub, sub) = TestSource.probe[Seq[Double]]
      .via(avgFlow)
      .toMat(TestSink.probe[AveragedValue])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(doubles)
    val avD = sub.expectNext()
    val mD = mean(doubles)
    assert(avD.count === doubles.size)
    assert(avD.value === (mD.right.get +- 0.005))
  }

  "An AveragedValue Flow of Floats" should "be near their mean" in {

    val (pub, sub) = TestSource.probe[Seq[Float]]
      .via(avgFlow)
      .toMat(TestSink.probe[AveragedValue])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(floats)
    val avF = sub.expectNext()
    val mF = mean(floats)
    assert(avF.count === floats.size)
    assert(avF.value === (mF.right.get.toDouble +- 0.005))
  }

  "An AveragedValue Flow of Ints" should "be near their mean" in {

    val (pub, sub) = TestSource.probe[Seq[Int]]
      .via(avgFlow)
      .toMat(TestSink.probe[AveragedValue])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(ints)
    val avI = sub.expectNext()
    val mI = mean(ints)
    assert(avI.count === ints.size)
    assert(avI.value === (mI.right.get.toDouble +- 0.5))
  }

  "An AveragedValue Flow of Longs" should "be near their mean" in {

    val (pub, sub) = TestSource.probe[Seq[Long]]
      .via(avgFlow)
      .toMat(TestSink.probe[AveragedValue])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(longs)
    val avL = sub.expectNext()
    val mL = mean(longs)
    assert(avL.count === ints.size)
    assert(avL.value === (mL.right.get.toDouble +- 0.5))
  }
}
