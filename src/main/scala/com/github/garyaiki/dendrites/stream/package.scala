/**

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
package com.github.garyaiki.dendrites

import _root_.akka.NotUsed
import _root_.akka.event.LoggingAdapter
import _root_.akka.stream.scaladsl.Flow
import scala.collection.mutable.ArrayBuffer
import com.github.garyaiki.dendrites.filters.filterRight
import com.github.garyaiki.dendrites.algebird.typeclasses.QTreeLike

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
  def flattenFlow[A: Ordering]: Flow[Seq[Option[A]], Seq[A], NotUsed] = Flow[Seq[Option[A]]].map(_.flatten)

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

  /** Wrap tuple3LeftRight into a Flow
    *
    * @return mapped tuple2
    */
  def leftRightFlow: Flow[(Either[String, AnyRef], Either[String, AnyRef], Either[String, AnyRef]),
    (Seq[String], Seq[AnyRef]), NotUsed] =
    Flow[(Either[String, AnyRef], Either[String, AnyRef], Either[String, AnyRef])].map(tuple3LeftRight)

  /** Map tuple3 from Zip stage to log failure and pass success results
    *
    * @param in tuple3 the result from 3 parallel calls
    * @return success results
    */
  def tuple3LogLeftRight(in: (Either[String, AnyRef], Either[String, AnyRef], Either[String, AnyRef]))
    (implicit logger: LoggingAdapter): Seq[AnyRef] = {

    val rights = new ArrayBuffer[AnyRef]()
    in._1 match {
      case Left(l)  => logger.warning(l)
      case Right(r) => rights += r
    }
    in._2 match {
      case Left(l)  => logger.warning(l)
      case Right(r) => rights += r
    }
    in._3 match {
      case Left(l)  => logger.warning(l)
      case Right(r) => rights += r
    }
    Seq(rights: _*)
  }

  /** Wrap tuple3LogLeftRight into a Flow
    *
    * @return Seq[AnyRef]
    */
  def logLeftRightFlow(implicit logger: LoggingAdapter):
    Flow[(Either[String, AnyRef], Either[String, AnyRef], Either[String, AnyRef]), Seq[AnyRef], NotUsed] =
      Flow[(Either[String, AnyRef], Either[String, AnyRef], Either[String, AnyRef])].map(tuple3LogLeftRight)
}
