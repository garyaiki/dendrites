/**
  */
package org.gs

import _root_.akka.NotUsed
import _root_.akka.event.LoggingAdapter
import _root_.akka.stream.scaladsl.Flow
import com.twitter.algebird._
import org.gs.filters._
import org.gs.algebird.typeclasses.QTreeLike
import scala.reflect.runtime.universe._

/** Akka Stream Flows
  *
  * @author Gary Struthers
  */
package object stream {

  /** Flow to flatten a sequence of Options
    *
    * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering Ordering]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.MaxFlowSpec]]
    * 
    * @tparam A elements that extend Ordering
    * @return values
    */
  def flattenFlow[A: Ordering]: Flow[Seq[Option[A]], Seq[A], NotUsed] =
          Flow[Seq[Option[A]]].map(_.flatten)

  /** Flow to collect the Right side value from a sequence of Either
    *
    * filterRight is converted to a Partial Function then passed to collect
    * 
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.MaxFlowSpec]]
    *
    * @tparam A Left
    * @tparam B Right
    * @return value contained in Right
    */
  def collectRightFlow[A, B]: Flow[Seq[Either[A, B]], Seq[B], NotUsed] =
          Flow[Seq[Either[A, B]]].collect(PartialFunction(filterRight))

  def filterRightLogLeft[A, B](in: Either[A, B])(implicit logger: LoggingAdapter): Boolean = in match {
    case Right(r) => true
    case Left(l) => logger.info(l.toString); false
  }
}
