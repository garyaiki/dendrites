/**
  */
package org.gs

import _root_.akka.NotUsed
import _root_.akka.event.LoggingAdapter
import _root_.akka.stream.scaladsl.Flow
import scala.collection.mutable.ArrayBuffer
import org.gs.filters.filterRight
import org.gs.algebird.typeclasses.QTreeLike

/** Akka Stream Flows
  *
  * Map sequence of Option to sequence of values
  * {{{
  * val (pub, sub) = TestSource.probe[Seq[Option[BigInt]]]
  *   .via(flattenFlow)
  *   .via(maxFlow)
  *   .toMat(TestSink.probe[BigInt])(Keep.both)
  *   .run()
  * }}}
  * Map sequence of Either to sequence of values
  * {{{
  * val (pub, sub) = TestSource.probe[Seq[Either[String, Double]]]
  *   .via(collectRightFlow)
  *   .via(maxFlow)
  *   .toMat(TestSink.probe[Double])(Keep.both)
  *   .run()
  * }}}
  * Flow accepts tuple3 from zip stage, wraps tuple3LeftRight which maps 3 Eithers to Sequence of
  * Lefts and a Sequence of Rights
  * {{{
  * def zipper = ZipWith((in0: Either[String, AnyRef],
  *   in1: Either[String, AnyRef],
  *   in2: Either[String, AnyRef]) => (in0, in1, in2))
  * val flowGraph = GraphDSL.create() { implicit builder =>
  *   val zip = builder.add(zipper)
  *   val fgLR = builder.add(leftRightFlow)
  * }}}
  * @see [[http://doc.akka.io/api/akka/2.4.6/#akka.stream.scaladsl.Flow Flow]]
  * @author Gary Struthers
  */
package object stream {

  /** Flow to flatten a sequence of Options
    *
    * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering Ordering]]
    * @tparam A elements that extend Ordering
    * @return values
    */
  def flattenFlow[A: Ordering]: Flow[Seq[Option[A]], Seq[A], NotUsed] =
          Flow[Seq[Option[A]]].map(_.flatten)

  /** Flow to collect the Right side value from a sequence of Either
    *
    * filterRight is converted to a Partial Function then passed to collect
    * @tparam A Left
    * @tparam B Right
    * @return value contained in Right
    */
  def collectRightFlow[A, B]: Flow[Seq[Either[A, B]], Seq[B], NotUsed] =
          Flow[Seq[Either[A, B]]].collect(PartialFunction(filterRight))

  /** Accept Rights, log Lefts */
  def filterRightLogLeft[A, B](in: Either[A, B])(implicit logger: LoggingAdapter): Boolean =
    in match {
    case Right(r) => true
    case Left(l) => logger.warning(l.toString); false
  }

  /** Map tuple3 from Zip stage to group failure and success results
    *
    * @param in tuple3 the result from 3 parallel calls
    * @return tuple2 all error strings and all success results
    */
  def tuple3LeftRight(in: (Either[String, AnyRef], Either[String, AnyRef], Either[String, AnyRef])):
          (Seq[String], Seq[AnyRef]) = {

    val lefts = new ArrayBuffer[String]()
    val rights = new ArrayBuffer[AnyRef]()
    in._1 match {
      case Left(l)  => lefts += l
      case Right(r) => rights += r
    }
    in._2 match {
      case Left(l)  => lefts += l
      case Right(r) => rights += r
    }
    in._3 match {
      case Left(l)  => lefts += l
      case Right(r) => rights += r
    }
    (Seq(lefts: _*), Seq(rights: _*))
  }

  /** Wrap tuple2LeftRight into a Flow
    *
    * @return mapped tuple2
    */
  def leftRightFlow: Flow[(Either[String, AnyRef], Either[String, AnyRef], Either[String, AnyRef]),
          (Seq[String], Seq[AnyRef]), NotUsed] =
    Flow[(Either[String, AnyRef], Either[String, AnyRef], Either[String, AnyRef])].
    map(tuple3LeftRight)
}
