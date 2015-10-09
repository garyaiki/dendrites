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
class SumAveragedFlowSpec extends FlatSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("akka-aggregator")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  "A sum of AveragedValues" should "be near the sum of their means" in {
    val avgBDFlow: Flow[Seq[BigDecimal], Seq[AveragedValue], Unit] =
            Flow[Seq[BigDecimal]].map(avg[BigDecimal]).grouped(2)
    val sumAvgBDFlow: Flow[Seq[AveragedValue], AveragedValue, Unit] =
            Flow[Seq[AveragedValue]].map(sumAverageValues)

    val (pubBD, subBD) = TestSource.probe[Seq[BigDecimal]]
      .via(avgBDFlow)
      .via(sumAvgBDFlow)
      .toMat(TestSink.probe[AveragedValue])(Keep.both)
      .run()
    subBD.request(2)
    pubBD.sendNext(bigDecimals)
    pubBD.sendNext(bigDecimals2)
    val avBD = subBD.expectNext()
    pubBD.sendComplete()
    subBD.expectComplete()
    assert(avBD.count === bigDecimals.size + bigDecimals2.size)
    val mBD = mean(bigDecimals ++ bigDecimals2)
    assert(avBD.value === (mBD.right.get.toDouble +- 0.005))
  }

  "A sum of AveragedValues" should "combine steps into 1 Flow" in {
    val avgSBDFlow: Flow[Seq[BigDecimal], AveragedValue, Unit] = 
            Flow[Seq[BigDecimal]].map(avg[BigDecimal]).grouped(2).map(sumAverageValues)

    val (pubBD, subBD) = TestSource.probe[Seq[BigDecimal]]
      .via(avgSBDFlow)
      .toMat(TestSink.probe[AveragedValue])(Keep.both)
      .run()
    subBD.request(2)
    pubBD.sendNext(bigDecimals)
    pubBD.sendNext(bigDecimals2)
    val avSBD = subBD.expectNext()
    pubBD.sendComplete()
    subBD.expectComplete()
    val mSBD = mean(bigDecimals ++ bigDecimals2)
    assert(avSBD.value === (mSBD.right.get.toDouble +- 0.005))
  }
}
