/**
  */
package org.gs.algebird.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Keep }
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import org.gs.aggregator._
import org.gs.algebird._
import org.gs.fixtures.{ CaseClassLike, TestValuesBuilder }
import com.twitter.algebird._

/** @author garystruthers
  *
  */
class AveragedFlowSpec extends FlatSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("akka-aggregator")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  "An AveragedValue Flow of BigDecimals" should "be near its mean" in {

    val (pubBD, subBD) = TestSource.probe[Seq[BigDecimal]]
      .via(avgBDFlow)
      .toMat(TestSink.probe[AveragedValue])(Keep.both)
      .run()
    subBD.request(1)
    pubBD.sendNext(bigDecimals)
    val avBD = subBD.expectNext()
    pubBD.sendComplete()
    subBD.expectComplete()
    assert(avBD.count === bigDecimals.size)
    val mBD = mean(bigDecimals)
    assert(avBD.value === (mBD.right.get.toDouble +- 0.005))
  }

  "An AveragedValue Flow of BigInts" should "be near its mean" in {

    val (pubBI, subBI) = TestSource.probe[Seq[BigInt]]
      .via(avgBIFlow)
      .toMat(TestSink.probe[AveragedValue])(Keep.both)
      .run()
    subBI.request(1)
    pubBI.sendNext(bigInts)
    val avBI = subBI.expectNext()
    val mBI = mean(bigInts)
    assert(avBI.count === bigInts.size)
    assert(avBI.value === (mBI.right.get.toDouble +- 0.5))
  }

  "An AveragedValue Flow of Doubles" should "be near their mean" in {

    val (pubD, subD) = TestSource.probe[Seq[Double]]
      .via(avgDFlow)
      .toMat(TestSink.probe[AveragedValue])(Keep.both)
      .run()
    subD.request(1)
    pubD.sendNext(doubles)
    val avD = subD.expectNext()
    val mD = mean(doubles)
    assert(avD.count === doubles.size)
    assert(avD.value === (mD.right.get +- 0.005))
  }

  "An AveragedValue Flow of Floats" should "be near their mean" in {

    val (pubF, subF) = TestSource.probe[Seq[Float]]
      .via(avgFFlow)
      .toMat(TestSink.probe[AveragedValue])(Keep.both)
      .run()
    subF.request(1)
    pubF.sendNext(floats)
    val avF = subF.expectNext()
    val mF = mean(floats)
    assert(avF.count === floats.size)
    assert(avF.value === (mF.right.get.toDouble +- 0.005))
  }

  "An AveragedValue Flow of Ints" should "be near their mean" in {

    val (pubI, subI) = TestSource.probe[Seq[Int]]
      .via(avgIFlow)
      .toMat(TestSink.probe[AveragedValue])(Keep.both)
      .run()
    subI.request(1)
    pubI.sendNext(ints)
    val avI = subI.expectNext()
    val mI = mean(ints)
    assert(avI.count === ints.size)
    assert(avI.value === (mI.right.get.toDouble +- 0.5))
  }

  "An AveragedValue Flow of Longs" should "be near their mean" in {

    val (pubL, subL) = TestSource.probe[Seq[Long]]
      .via(avgLFlow)
      .toMat(TestSink.probe[AveragedValue])(Keep.both)
      .run()
    subL.request(1)
    pubL.sendNext(longs)
    val avL = subL.expectNext()
    val mL = mean(longs)
    assert(avL.count === ints.size)
    assert(avL.value === (mL.right.get.toDouble +- 0.5))
  }

}
