/**
  */
package org.gs.algebird

import akka.stream.scaladsl.Flow
import com.twitter.algebird._
import org.gs.filters._
import scala.reflect.runtime.universe._

/** @author garystruthers
  *
  */
package object stream {

  /** Flow to average sequence of Numerics. When Numerics are ambiguous use typed Flows below
    *
    * @return AveragedValue
    */
  def avgFlow[A: TypeTag: Numeric]: Flow[Seq[A], AveragedValue, Unit] = Flow[Seq[A]].map(avg[A])

  /** Flow to sum sequences of AveragedValues to a single AveragedValue
    *
    * @return AveragedValue
    */
  def sumAvgFlow: Flow[Seq[AveragedValue], AveragedValue, Unit] =
    Flow[Seq[AveragedValue]].map(sumAverageValues)

  /** Flow to estimate size of HLL
    *
    * @return estimated size of HLL
    */
  def estSizeFlow: Flow[HLL, Double, Unit] = Flow[HLL].map(x => x.estimatedSize)

  /** Flow to map HLL to Approximate[Long]
    *
    * @return Approximate[Long]
    */
  def toApproximate: Flow[HLL, Approximate[Long], Unit] = Flow[HLL].map(x => x.approximateSize)

  /** Flow to map sequence of HLL to sequence of Approximate[Long]
    *
    * @return Approximate[Long]
    */
  def toApproximates: Flow[Seq[HLL], Seq[Approximate[Long]], Unit] =
    Flow[Seq[HLL]].map(mapHLL2Approximate)

  /** Flow to sum sequence of HLL to an Approximate[Long]
    *
    * @return Approximate[Long]
    */
  def sumHLLs: Flow[Seq[HLL], Approximate[Long], Unit] = Flow[Seq[HLL]].map(sumHLLApproximateSizes)

  def maxFlow[A: Ordering]: Flow[Seq[A], A, Unit] =
    Flow[Seq[A]].map(max[A])

  def minFlow[A: Ordering]: Flow[Seq[A], A, Unit] =
    Flow[Seq[A]].map(min[A])

  def flattenFlow[A: Ordering]: Flow[Seq[Option[A]], Seq[A], Unit] =
    Flow[Seq[Option[A]]].map(_.flatten)

  def collectRightFlow[A, B]: Flow[Seq[Either[A, B]], Seq[B], Unit] =
    Flow[Seq[Either[A, B]]].collect(PartialFunction(filterRight))

  /** Flow to find average of sequence of BigDecimals
    *
    * @return AveragedValue
    */
  def avgBDFlow: Flow[Seq[BigDecimal], AveragedValue, Unit] =
    Flow[Seq[BigDecimal]].map(avg[BigDecimal])

  /** Flow to find average of sequence of BigInts
    *
    * @return AveragedValue
    */
  def avgBIFlow: Flow[Seq[BigInt], AveragedValue, Unit] = Flow[Seq[BigInt]].map(avg[BigInt])

  /** Flow to find average of sequence of Doubles
    *
    * @return AveragedValue
    */
  def avgDFlow: Flow[Seq[Double], AveragedValue, Unit] = Flow[Seq[Double]].map(avg[Double])

  /** Flow to find average of sequence of Floats
    *
    * @return AveragedValue
    */
  def avgFFlow: Flow[Seq[Float], AveragedValue, Unit] = Flow[Seq[Float]].map(avg[Float])

  /** Flow to find average of sequence of Ints
    *
    * @return AveragedValue
    */
  def avgIFlow: Flow[Seq[Int], AveragedValue, Unit] = Flow[Seq[Int]].map(avg[Int])

  /** Flow to find average of sequence of Longs
    *
    * @return AveragedValue
    */
  def avgLFlow: Flow[Seq[Long], AveragedValue, Unit] = Flow[Seq[Long]].map(avg[Long])

}