/**
  */
package org.gs.algebird

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.twitter.algebird.{Approximate, AveragedValue, HLL, QTree}
import scala.reflect.runtime.universe.TypeTag
import org.gs.algebird.typeclasses.QTreeLike

/** Akka Stream Flows for Algebird hashing and aggregating functions
  *
  * ApproximatorsFlow class broadcasts to parallel Akka Agents that contain Algebird aggregators and
  * a Zip stage waits form them to complete
  *
  * CreateCMSFlow class initializes a CMS monoid then creates Count Mean Sketch for value
  *
  * CreateHLLFLow class initializes a HyperLogLogAggregator then HyperLogLog for value
  *
  * ZipTimeStage class creates a tuple of received value and current time in milliseconds
  * DecayedValue uses these value/time pairs
  *
  * == AveragedValue ==
  * Average sequence of Numerics Flow `org.gs.algebird.stream.AveragedFlowSpec`
  * {{{
  * val avg: FlowShape[Seq[A], AveragedValue] = builder.add(avgFlow)
  * }}}
  * Sum AveragedValues sequences to AveragedValue `org.gs.algebird.stream.SumAveragedFlowSpec`
  * {{{
  * val (pub, sub) = TestSource.probe[Seq[BigDecimal]]
  *  .via(avgBDFlow.grouped(2))
  *  .via(sumAvgFlow)
  *  .toMat(TestSink.probe[AveragedValue])(Keep.both)
  *  .run()
  * }}}
  * == Hyper Log Log == `org.gs.algebird.stream.HyperLogLogFlowSpec`
  * Estimate size of Hyper Log Log
  * {{{
  * val (pub, sub) = TestSource.probe[HLL]
  *  .via(estSizeFlow)
  *  .toMat(TestSink.probe[Double])(Keep.both)
  *  .run()
  * }}}
  * Map Hyper Log Log to Approximate[Long]
  * {{{
  * val (pub, sub) = TestSource.probe[HLL]
  *  .via(toApproximate)
  *  .toMat(TestSink.probe[Approximate[Long]])(Keep.both)
  *  .run()
  * }}}
  * Map Hyper Log Log sequence to Approximate[Long] sequence
  * {{{
  * val (pub, sub) = TestSource.probe[Seq[HLL]]
  * .via(toApproximates)
  * .toMat(TestSink.probe[Seq[Approximate[Long]]])(Keep.both)
  * .run()
  * }}}
  * Sum Hyper Log Log sequence to Approximate[Long]
  * {{{
  * val (pub, sub) = TestSource.probe[Seq[HLL]]
  *  .via(sumHLLs)
  *  .toMat(TestSink.probe[Approximate[Long]])(Keep.both)
  *  .run()
  * }}}
  * == Max value in a sequence == `org.gs.algebird.stream.MaxFlowSpec`
  * {{{
  * val (pub, sub) = TestSource.probe[Seq[BigDecimal]]
  *  .via(maxFlow)
  *  .toMat(TestSink.probe[BigDecimal])(Keep.both)
  *  .run()
  }}}
  * == Min value in a sequence == `org.gs.algebird.stream.MinFlowSpec`
  * {{{
  * val (pub, sub) = TestSource.probe[Seq[BigInt]]
  *  .via(minFlow)
  *  .toMat(TestSink.probe[BigInt])(Keep.both)
  *  .run()
  * }}}
  * == QTree == `org.gs.algebird.stream.QTreeFlowSpec`
  * Find max value in a QTree 
  * {{{
  * def sourceBI = TestSource.probe[QTree[BigInt]]
  * def sinkD = TestSink.probe[Double]
  * val (pub, sub) = sourceBI
  *  .via(qTreeMaxFlow)
  *  .toMat(sinkD)(Keep.both)
  *  .run()
  * }}}
  * Find min value in a QTree
  * {{{
  * def sourceBD = TestSource.probe[QTree[BigDecimal]]
  * def sinkD = TestSink.probe[Double]
  * val (pub, sub) = sourceBD
  *  .via(qTreeMinFlow)
  *  .toMat(sinkD)(Keep.both)
  *  .run()
  * }}}
  * Find first quartile bounds in a QTree
  * {{{
  * def sourceD = TestSource.probe[QTree[Double]]
  * def sinkDD = TestSink.probe[(Double, Double)]
  * val (pub, sub) = sourceD
  *  .via(firstQuartileFlow)
  *  .toMat(sinkDD)(Keep.both)
  *  .run()
  * }}}
  * Find second quartile bounds in a QTree
  * {{{
  * def sourceF = TestSource.probe[QTree[Float]]
  * def sinkDD = TestSink.probe[(Double, Double)]
  * val (pub, sub) = sourceF
  *  .via(secondQuartileFlow)
  *  .toMat(sinkDD)(Keep.both)
  *  .run()
  * }}}
  * Find third quartile bounds in a QTree
  * {{{
  * def sourceI = TestSource.probe[QTree[Int]]
  * def sinkDD = TestSink.probe[(Double, Double)]
  * val (pub, sub) = sourceI
  *  .via(thirdQuartileFlow)
  *  .toMat(sinkDD)(Keep.both)
  *  .run()
  * }}}
  * Find inter quartile mean in a QTree[Long]
  * {{{
  * def sourceL = TestSource.probe[QTree[Long]]
  * def sinkDD = TestSink.probe[(Double, Double)]
  * val (pub, sub) = sourceL
  *  .via(interQuartileMeanLFlow)
  *  .toMat(sinkDD)(Keep.both)
  *  .run()
  * }}}
  * @author Gary Struthers
  * @see [[org.gs.algebird]] package object for the functions these Flows wrap
  */
package object stream {

  /** Flow to average sequence of Numerics. When Numerics are ambiguous use typed avg Flows below
    *
    * @see [[http://www.scala-lang.org/api/2.11.7/scala-reflect/#scala.reflect.api.TypeTags]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @tparam A can be generic by including TypeTage at runtime & if it's sole Numeric in scope
    * @return AveragedValue
    */
  def avgFlow[A: TypeTag: Numeric]: Flow[Seq[A], AveragedValue, NotUsed] = Flow[Seq[A]].map(avg[A])

  /** Flow to find average of sequence of BigDecimals
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return AveragedValue
    */
  def avgBDFlow: Flow[Seq[BigDecimal], AveragedValue, NotUsed] =
    Flow[Seq[BigDecimal]].map(avg[BigDecimal])

  /** Flow to find average of sequence of BigInts
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return AveragedValue
    */
  def avgBIFlow: Flow[Seq[BigInt], AveragedValue, NotUsed] = Flow[Seq[BigInt]].map(avg[BigInt])

  /** Flow to find average of sequence of Doubles
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return AveragedValue
    */
  def avgDFlow: Flow[Seq[Double], AveragedValue, NotUsed] = Flow[Seq[Double]].map(avg[Double])

  /** Flow to find average of sequence of Floats
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return AveragedValue
    */
  def avgFFlow: Flow[Seq[Float], AveragedValue, NotUsed] = Flow[Seq[Float]].map(avg[Float])

  /** Flow to find average of sequence of Ints
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return AveragedValue
    */
  def avgIFlow: Flow[Seq[Int], AveragedValue, NotUsed] = Flow[Seq[Int]].map(avg[Int])

  /** Flow to find average of sequence of Longs
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return AveragedValue
    */
  def avgLFlow: Flow[Seq[Long], AveragedValue, NotUsed] = Flow[Seq[Long]].map(avg[Long])

  /** Flow to sum sequences of AveragedValues to a single AveragedValue
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return AveragedValue
    */
  def sumAvgFlow: Flow[Seq[AveragedValue], AveragedValue, NotUsed] =
    Flow[Seq[AveragedValue]].map(sumAverageValues)

  /** Flow to estimate size of HLL
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL HLL]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return estimated size of HLL
    */
  def estSizeFlow: Flow[HLL, Double, NotUsed] = Flow[HLL].map(x => x.estimatedSize)

  /** Flow to map Hyper Log Log to Approximate[Long]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL HLL]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Approximate Approximate]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return Approximate[Long]
    */
  def toApproximate: Flow[HLL, Approximate[Long], NotUsed] = Flow[HLL].map(x => x.approximateSize)

  /** Flow to map sequence of HLL to sequence of Approximate[Long]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL HLL]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Approximate Approximate]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return Approximate[Long]
    */
  def toApproximates: Flow[Seq[HLL], Seq[Approximate[Long]], NotUsed] =
    Flow[Seq[HLL]].map(mapHLL2Approximate)

  /** Flow to sum sequence of HLL to an Approximate[Long]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL HLL]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Approximate Approximate]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return Approximate[Long]
    */
  def sumHLLs: Flow[Seq[HLL], Approximate[Long], NotUsed] = Flow[Seq[HLL]].map(sumHLLApproximateSizes)

  /** Flow to find max value in a sequence
    *
    * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering Ordering]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return max
    */
  def maxFlow[A: Ordering]: Flow[Seq[A], A, NotUsed] = Flow[Seq[A]].map(max[A])

  /** Flow to find min value in a sequence
    *
    * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering Ordering]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return min
    */
  def minFlow[A: Ordering]: Flow[Seq[A], A, NotUsed] = Flow[Seq[A]].map(min[A])

  /** Flow to find max value in a QTree
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[org.gs.algebird.typeclasses.QTreeLike]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @tparam A QTreeLike typeclass that construct QTree[A]
    * @return max
    */
  def qTreeMaxFlow[A: QTreeLike]: Flow[QTree[A], Double, NotUsed] = Flow[QTree[A]].map(_.upperBound)

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
  def qTreeMinFlow[A: QTreeLike]: Flow[QTree[A], Double, NotUsed] = Flow[QTree[A]].map(_.lowerBound)

  /** Flow to find first quartile bounds in a QTree
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[org.gs.algebird.typeclasses.QTreeLike]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @tparam A QTreeLike typeclass that construct QTree[A]
    * @return lower, upper bounds
    */
  def firstQuartileFlow[A: QTreeLike]: Flow[QTree[A], (Double, Double), NotUsed] = {
    Flow[QTree[A]].map(_.quantileBounds(0.25))
  }

  /** Flow to find second quartile bounds in a QTree
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[org.gs.algebird.typeclasses.QTreeLike]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @tparam A QTreeLike typeclass that construct QTree[A]
    * @return lower, upper bounds
    */
  def secondQuartileFlow[A: QTreeLike]: Flow[QTree[A], (Double, Double), NotUsed] = {
    Flow[QTree[A]].map(_.quantileBounds(0.5))
  }

  /** Flow to find third quartile bounds in a QTree
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[org.gs.algebird.typeclasses.QTreeLike]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @tparam A QTreeLike typeclass that construct QTree[A]
    * @return lower, upper bounds
    */
  def thirdQuartileFlow[A: QTreeLike]: Flow[QTree[A], (Double, Double), NotUsed] = {
    Flow[QTree[A]].map(_.quantileBounds(0.75))
  }

  /** Flow to find inter quartile mean in a QTree[BigDecimal]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanBDFlow: Flow[QTree[BigDecimal], (Double, Double), NotUsed] = {
    Flow[QTree[BigDecimal]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[BigInt]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanBIFlow: Flow[QTree[BigInt], (Double, Double), NotUsed] = {
    Flow[QTree[BigInt]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[Double]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanDFlow: Flow[QTree[Double], (Double, Double), NotUsed] = {
    Flow[QTree[Double]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[Float]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanFFlow: Flow[QTree[Float], (Double, Double), NotUsed] = {
    Flow[QTree[Float]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[Int]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanIFlow: Flow[QTree[Int], (Double, Double), NotUsed] = {
    Flow[QTree[Int]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[Long]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/#akka.stream.scaladsl.Flow]]
    * @return mean of middle 50th percentile
    */
  def interQuartileMeanLFlow: Flow[QTree[Long], (Double, Double), NotUsed] = {
    Flow[QTree[Long]].map(_.interQuartileMean)
  }
}
