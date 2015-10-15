/**
  */
package org.gs.algebird

import akka.stream.scaladsl.Flow
import com.twitter.algebird._
import org.gs.filters._
import org.gs.algebird.typeclasses.QTreeLike
import scala.reflect.runtime.universe._

/** @author garystruthers
  *
  */
package object stream {

  /** Flow to flatten a sequence of Options
    *
    * @return values
    */
  def flattenFlow[A: Ordering]: Flow[Seq[Option[A]], Seq[A], Unit] =
    Flow[Seq[Option[A]]].map(_.flatten)

  /** Flow to collect the Right side value from a sequence of Either
    *
    * @return Right side values
    */
  def collectRightFlow[A, B]: Flow[Seq[Either[A, B]], Seq[B], Unit] =
    Flow[Seq[Either[A, B]]].collect(PartialFunction(filterRight))

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

  /** Flow to find max value in a sequence
    *
    * @return max
    */
  def maxFlow[A: Ordering]: Flow[Seq[A], A, Unit] =
    Flow[Seq[A]].map(max[A])

  /** Flow to find min value in a sequence
    *
    * @return min
    */
  def minFlow[A: Ordering]: Flow[Seq[A], A, Unit] =
    Flow[Seq[A]].map(min[A])

  /** Flow to find max value in a QTree
    *
    * @return max
    */
  def qTreeMaxFlow[A: QTreeLike]: Flow[QTree[A], Double, Unit] = Flow[QTree[A]].map(_.upperBound)

  /** Flow to find min value in a QTree
    *
    * @return min
    */
  def qTreeMinFlow[A: QTreeLike]: Flow[QTree[A], Double, Unit] = Flow[QTree[A]].map(_.lowerBound)

  /** Flow to find first quartile bounds in a QTree
    *
    * @return lower, upper bounds
    */
  def firstQuartileFlow[A: QTreeLike]: Flow[QTree[A], (Double, Double), Unit] = {
    Flow[QTree[A]].map(_.quantileBounds(0.25))
  }

  /** Flow to find second quartile bounds in a QTree
    *
    * @return lower, upper bounds
    */
  def secondQuartileFlow[A: QTreeLike]: Flow[QTree[A], (Double, Double), Unit] = {
    Flow[QTree[A]].map(_.quantileBounds(0.5))
  }

  /** Flow to find third quartile bounds in a QTree
    *
    * @return lower, upper bounds
    */
  def thirdQuartileFlow[A: QTreeLike]: Flow[QTree[A], (Double, Double), Unit] = {
    Flow[QTree[A]].map(_.quantileBounds(0.75))
  }

  /** Flow to find inter quartile mean in a QTree[BigDecimal]
    *
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanBDFlow: Flow[QTree[BigDecimal], (Double, Double), Unit] = {
    Flow[QTree[BigDecimal]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[BigInt]
    *
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanBIFlow: Flow[QTree[BigInt], (Double, Double), Unit] = {
    Flow[QTree[BigInt]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[Double]
    *
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanDFlow: Flow[QTree[Double], (Double, Double), Unit] = {
    Flow[QTree[Double]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[Float]
    *
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanFFlow: Flow[QTree[Float], (Double, Double), Unit] = {
    Flow[QTree[Float]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[Int]
    *
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanIFlow: Flow[QTree[Int], (Double, Double), Unit] = {
    Flow[QTree[Int]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[Long]
    *
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanLFlow: Flow[QTree[Long], (Double, Double), Unit] = {
    Flow[QTree[Long]].map(_.interQuartileMean)
  }

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