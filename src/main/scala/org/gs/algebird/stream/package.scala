/**
  */
package org.gs.algebird

import akka.stream.scaladsl.Flow
import com.twitter.algebird._
import org.gs.algebird.typeclasses.QTreeLike
import scala.reflect.runtime.universe._

/** Akka Stream Flows for Algebird hashing and aggregating functions
  *
  * @author garystruthers
  * @see [[org.gs.algebird]] package object for the functions these Flows wrap
  */
package object stream {

  /** Flow to average sequence of Numerics. When Numerics are ambiguous use typed avg Flows below
    *
    * @see [[http://www.scala-lang.org/api/2.11.7/scala-reflect/#scala.reflect.api.TypeTags]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.AveragedFlowSpec]]
    *
    * @tparam A can be generic by including TypeTage at runtime & if it's sole Numeric in scope
    * @return AveragedValue
    */
  def avgFlow[A: TypeTag: Numeric]: Flow[Seq[A], AveragedValue, Unit] = Flow[Seq[A]].map(avg[A])

  /** Flow to find average of sequence of BigDecimals
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.SumAveragedFlowSpec]]
    *
    * @return AveragedValue
    */
  def avgBDFlow: Flow[Seq[BigDecimal], AveragedValue, Unit] =
    Flow[Seq[BigDecimal]].map(avg[BigDecimal])

  /** Flow to find average of sequence of BigInts
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.SumAveragedFlowSpec]]
    *
    * @return AveragedValue
    */
  def avgBIFlow: Flow[Seq[BigInt], AveragedValue, Unit] = Flow[Seq[BigInt]].map(avg[BigInt])

  /** Flow to find average of sequence of Doubles
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.SumAveragedFlowSpec]]
    *
    * @return AveragedValue
    */
  def avgDFlow: Flow[Seq[Double], AveragedValue, Unit] = Flow[Seq[Double]].map(avg[Double])

  /** Flow to find average of sequence of Floats
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.SumAveragedFlowSpec]]
    *
    * @return AveragedValue
    */
  def avgFFlow: Flow[Seq[Float], AveragedValue, Unit] = Flow[Seq[Float]].map(avg[Float])

  /** Flow to find average of sequence of Ints
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.SumAveragedFlowSpec]]
    *
    * @return AveragedValue
    */
  def avgIFlow: Flow[Seq[Int], AveragedValue, Unit] = Flow[Seq[Int]].map(avg[Int])

  /** Flow to find average of sequence of Longs
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.SumAveragedFlowSpec]]
    *
    * @return AveragedValue
    */
  def avgLFlow: Flow[Seq[Long], AveragedValue, Unit] = Flow[Seq[Long]].map(avg[Long])

  /** Flow to sum sequences of AveragedValues to a single AveragedValue
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.SumAveragedFlowSpec]]
    * 
    * @return AveragedValue
    */
  def sumAvgFlow: Flow[Seq[AveragedValue], AveragedValue, Unit] =
    Flow[Seq[AveragedValue]].map(sumAverageValues)

  /** Flow to estimate size of HLL
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL HLL]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.HyperLogLogFlowSpec]]
    * 
    * @return estimated size of HLL
    */
  def estSizeFlow: Flow[HLL, Double, Unit] = Flow[HLL].map(x => x.estimatedSize)

  /** Flow to map HLL to Approximate[Long]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL HLL]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Approximate Approximate]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.HyperLogLogFlowSpec]]
    *     
    * @return Approximate[Long]
    */
  def toApproximate: Flow[HLL, Approximate[Long], Unit] = Flow[HLL].map(x => x.approximateSize)

  /** Flow to map sequence of HLL to sequence of Approximate[Long]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL HLL]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Approximate Approximate]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.HyperLogLogFlowSpec]]
    *     
    * @return Approximate[Long]
    */
  def toApproximates: Flow[Seq[HLL], Seq[Approximate[Long]], Unit] =
    Flow[Seq[HLL]].map(mapHLL2Approximate)

  /** Flow to sum sequence of HLL to an Approximate[Long]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL HLL]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Approximate Approximate]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.HyperLogLogFlowSpec]]
    *
    * @return Approximate[Long]
    */
  def sumHLLs: Flow[Seq[HLL], Approximate[Long], Unit] = Flow[Seq[HLL]].map(sumHLLApproximateSizes)

  /** Flow to find max value in a sequence
    *
    * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering Ordering]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.MaxFlowSpec]]
    *    
    * @return max
    */
  def maxFlow[A: Ordering]: Flow[Seq[A], A, Unit] = Flow[Seq[A]].map(max[A])

  /** Flow to find min value in a sequence
    *
    * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering Ordering]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.MinFlowSpec]]
    *
    * @return min
    */
  def minFlow[A: Ordering]: Flow[Seq[A], A, Unit] = Flow[Seq[A]].map(min[A])

  /** Flow to find max value in a QTree
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[org.gs.algebird.typeclasses.QTreeLike]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.QTreeFlowSpec]]
    *
    * @tparam A QTreeLike typeclass that construct QTree[A]
    * @return max
    */
  def qTreeMaxFlow[A: QTreeLike]: Flow[QTree[A], Double, Unit] = Flow[QTree[A]].map(_.upperBound)

  /** Flow to find min value in a QTree
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[org.gs.algebird.typeclasses.QTreeLike]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.QTreeFlowSpec]]
    *
    * @tparam A QTreeLike typeclass that construct QTree[A]
    * @return min
    */
  def qTreeMinFlow[A: QTreeLike]: Flow[QTree[A], Double, Unit] = Flow[QTree[A]].map(_.lowerBound)

  /** Flow to find first quartile bounds in a QTree
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[org.gs.algebird.typeclasses.QTreeLike]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.QTreeFlowSpec]]
    *
    * @tparam A QTreeLike typeclass that construct QTree[A]
    * @return lower, upper bounds
    */
  def firstQuartileFlow[A: QTreeLike]: Flow[QTree[A], (Double, Double), Unit] = {
    Flow[QTree[A]].map(_.quantileBounds(0.25))
  }

  /** Flow to find second quartile bounds in a QTree
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[org.gs.algebird.typeclasses.QTreeLike]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.QTreeFlowSpec]]
    *
    * @tparam A QTreeLike typeclass that construct QTree[A]
    * @return lower, upper bounds
    */
  def secondQuartileFlow[A: QTreeLike]: Flow[QTree[A], (Double, Double), Unit] = {
    Flow[QTree[A]].map(_.quantileBounds(0.5))
  }

  /** Flow to find third quartile bounds in a QTree
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[org.gs.algebird.typeclasses.QTreeLike]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.QTreeFlowSpec]]
    *
    * @tparam A QTreeLike typeclass that construct QTree[A]
    * @return lower, upper bounds
    */
  def thirdQuartileFlow[A: QTreeLike]: Flow[QTree[A], (Double, Double), Unit] = {
    Flow[QTree[A]].map(_.quantileBounds(0.75))
  }

  /** Flow to find inter quartile mean in a QTree[BigDecimal]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.QTreeFlowSpec]]
    *
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanBDFlow: Flow[QTree[BigDecimal], (Double, Double), Unit] = {
    Flow[QTree[BigDecimal]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[BigInt]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.QTreeFlowSpec]]
    *
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanBIFlow: Flow[QTree[BigInt], (Double, Double), Unit] = {
    Flow[QTree[BigInt]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[Double]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.QTreeFlowSpec]]
    *
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanDFlow: Flow[QTree[Double], (Double, Double), Unit] = {
    Flow[QTree[Double]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[Float]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.QTreeFlowSpec]]
    *
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanFFlow: Flow[QTree[Float], (Double, Double), Unit] = {
    Flow[QTree[Float]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[Int]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.QTreeFlowSpec]]
    *
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanIFlow: Flow[QTree[Int], (Double, Double), Unit] = {
    Flow[QTree[Int]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[Long]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @example [[org.gs.algebird.stream.QTreeFlowSpec]]
    *
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanLFlow: Flow[QTree[Long], (Double, Double), Unit] = {
    Flow[QTree[Long]].map(_.interQuartileMean)
  }

}