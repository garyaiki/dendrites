/**
  */
package org.gs.algebird.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import org.scalatest.{ FlatSpecLike, Matchers }
import org.gs.algebird._
import org.gs.fixtures.TestValuesBuilder
import org.gs.stream._

/** @author garystruthers
  *
  */
class MaxFlowSpec extends FlatSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("akka-aggregator")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  "A Sequence of BigDecimal" should "return its Max" in {
    val (pub, sub) = TestSource.probe[Seq[BigDecimal]]
      .via(maxFlow)
      .toMat(TestSink.probe[BigDecimal])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(bigDecimals)
    sub.expectNext(bigDecimals.max)
    pub.sendComplete()
    sub.expectComplete()
  }

  "A Sequence of String" should "return its Max" in {
    val (pub, sub) = TestSource.probe[Seq[String]]
      .via(maxFlow)
      .toMat(TestSink.probe[String])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(strings)
    sub.expectNext(strings.max)
    pub.sendComplete()
    sub.expectComplete()
  }

  "A Sequence of Option[BigInt]" should "return its Max" in {
    val (pub, sub) = TestSource.probe[Seq[Option[BigInt]]]
      .via(flattenFlow)
      .via(maxFlow)
      .toMat(TestSink.probe[BigInt])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(optBigInts)
    sub.expectNext(optBigInts.flatten.max)
    pub.sendComplete()
    sub.expectComplete()
  }

  "A Sequence of Either[String, Double]" should "return its Max" in {
    val (pub, sub) = TestSource.probe[Seq[Either[String, Double]]]
      .via(collectRightFlow)
      .via(maxFlow)
      .toMat(TestSink.probe[Double])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(eithDoubles)
    sub.expectNext(doubles.max)
    pub.sendComplete()
    sub.expectComplete()
  }
}
