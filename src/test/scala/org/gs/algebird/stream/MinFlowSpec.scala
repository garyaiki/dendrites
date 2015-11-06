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
class MinFlowSpec extends FlatSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  "A Sequence of BigInt" should "return its Min" in {
    val (pub, sub) = TestSource.probe[Seq[BigInt]]
      .via(minFlow)
      .toMat(TestSink.probe[BigInt])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(bigInts)
    sub.expectNext(bigInts.min)
    pub.sendComplete()
    sub.expectComplete()
    assert(min(bigInts) === bigInts.min)
  }

  "A Sequence of Option[Double]" should "return its Min" in {
    val (pub, sub) = TestSource.probe[Seq[Option[Double]]]
      .via(flattenFlow)
      .via(minFlow)
      .toMat(TestSink.probe[Double])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(optDoubles)
    sub.expectNext(optDoubles.flatten.min)
    pub.sendComplete()
    sub.expectComplete()
  }

  "A Sequence of Either[String, Float]" should "return its Min" in {
    val (pub, sub) = TestSource.probe[Seq[Either[String, Float]]]
      .via(collectRightFlow)
      .via(minFlow)
      .toMat(TestSink.probe[Float])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(eithFloats)
    sub.expectNext(floats.min)
    pub.sendComplete()
    sub.expectComplete()
  }
}
