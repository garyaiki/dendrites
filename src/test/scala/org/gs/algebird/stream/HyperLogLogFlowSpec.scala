/**
  */
package org.gs.algebird.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Keep}
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import com.twitter.algebird._
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import org.gs.algebird.createHLL
import org.gs.algebird.typeclasses.HyperLogLogLike
import org.gs.fixtures.TestValuesBuilder

/** @author Gary Struthers
  *
  */
class HyperLogLogFlowSpec extends FlatSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val ag = HyperLogLogAggregator(12)
  val hll = createHLL(ints)
  val hll2 = createHLL(ints2)
  val hllV = Vector(hll, hll2)

  "A HyperLogLog Flow" should "estimate number of distinct integers from a Sequence of Int" in {
    val (pub, sub) = TestSource.probe[HLL]
      .via(estSizeFlow)
      .toMat(TestSink.probe[Double])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(hll)
    val size = sub.expectNext()
    pub.sendComplete()
    sub.expectComplete()
    assert(size === (ints.distinct.size.toDouble +- 0.09))
  }

  it should "map an HLL to an Approximate" in {
    val (pub, sub) = TestSource.probe[HLL]
      .via(toApproximate)
      .toMat(TestSink.probe[Approximate[Long]])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(hll)
    val approx = sub.expectNext()
    pub.sendComplete()
    sub.expectComplete()
    assert(approx.estimate === ints.size)
  }

  it should "map a Sequence of HLL to a Sequence of Approximate" in {
    val (pub, sub) = TestSource.probe[Seq[HLL]]
      .via(toApproximates)
      .toMat(TestSink.probe[Seq[Approximate[Long]]])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(hllV)
    val approxs = sub.expectNext()
    pub.sendComplete()
    sub.expectComplete()
    val sum = approxs.reduce(_ + _)
    assert(sum.estimate === ints.size + ints2.size)
  }

  it should "sum a Sequence of HLL to an Approximate" in {

    val (pub, sub) = TestSource.probe[Seq[HLL]]
      .via(sumHLLs)
      .toMat(TestSink.probe[Approximate[Long]])(Keep.both)
      .run()
    sub.request(1)
    pub.sendNext(hllV)
    val sum = sub.expectNext()
    pub.sendComplete()
    sub.expectComplete()
    assert(sum.estimate === ints.size + ints2.size)
  }
}
