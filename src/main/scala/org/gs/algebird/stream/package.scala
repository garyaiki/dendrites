/**
  */
package org.gs.algebird

import akka.stream.scaladsl.Flow
import com.twitter.algebird._

/** @author garystruthers
  *
  */
package object stream {

  /** Reusable Flow to find average of sequence of BigDecimals
    *
    * @return AveragedValue
    */
  def avgBDFlow: Flow[Seq[BigDecimal], AveragedValue, Unit] =
    Flow[Seq[BigDecimal]].map(avg[BigDecimal])

  /** Reusable Flow to find average of sequence of BigInts
    *
    * @return AveragedValue
    */
  def avgBIFlow: Flow[Seq[BigInt], AveragedValue, Unit] = Flow[Seq[BigInt]].map(avg[BigInt])

  /** Reusable Flow to find average of sequence of Doubles
    *
    * @return AveragedValue
    */
  def avgDFlow: Flow[Seq[Double], AveragedValue, Unit] = Flow[Seq[Double]].map(avg[Double])

  /** Reusable Flow to find average of sequence of Floats
    *
    * @return AveragedValue
    */
  def avgFFlow: Flow[Seq[Float], AveragedValue, Unit] = Flow[Seq[Float]].map(avg[Float])

  /** Reusable Flow to find average of sequence of Ints
    *
    * @return AveragedValue
    */
  def avgIFlow: Flow[Seq[Int], AveragedValue, Unit] = Flow[Seq[Int]].map(avg[Int])

  /** Reusable Flow to find average of sequence of Longs
    *
    * @return AveragedValue
    */
  def avgLFlow: Flow[Seq[Long], AveragedValue, Unit] = Flow[Seq[Long]].map(avg[Long])

  /** Reusable Flow to estimate size of HLL
    *
    * @return estimated size of HLL
    */
  def estSizeFlow: Flow[HLL, Double, Unit] = Flow[HLL].map(x => x.estimatedSize)

  /** Reusable Flow to map HLL to Approximate[Long]
    *
    * @return Approximate[Long]
    */
  def toApproximate: Flow[HLL, Approximate[Long], Unit] = Flow[HLL].map(x => x.approximateSize)

  /** Reusable Flow to map sequence of HLL to sequence of Approximate[Long]
    *
    * @return Approximate[Long]
    */
  def toApproximates: Flow[Seq[HLL], Seq[Approximate[Long]], Unit] =
    Flow[Seq[HLL]].map(mapHLL2Approximate)

  /** Reusable Flow to sum sequence of HLL to an Approximate[Long]
    *
    * @return Approximate[Long]
    */
  def sumHLLs: Flow[Seq[HLL], Approximate[Long], Unit] = Flow[Seq[HLL]].map(sumHLLApproximateSizes)
}