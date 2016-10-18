/** Copyright 2016 Gary Struthers

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
package org.gs.algebird

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.twitter.algebird.{Approximate, AveragedValue, HLL, QTree}
import scala.reflect.runtime.universe.TypeTag
import org.gs.algebird.typeclasses.QTreeLike

/** Akka Stream Flows for Algebird hashing and aggregating functions
  *
  * CreateCMSFlow class initializes a CMS monoid then creates Count Mean Sketch for value
  *
  * CreateHLLFLow class initializes a HyperLogLogAggregator then HyperLogLog for value
  *
  * ZipTimeFlow class creates a tuple of received value and current time in milliseconds
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
  * @see [[org.gs.algebird]] package object for the functions these Flows wrap
  * @author Gary Struthers
  */
package object stream {

  /** Flow to average sequence of Numerics. When Numerics are ambiguous use typed avg Flows below
    *
    * @tparam A can be generic by including TypeTage at runtime & if it's sole Numeric in scope
    * @return AveragedValue
    *
    * @see [[http://www.scala-lang.org/api/current/scala-reflect/#scala.reflect.api.TypeTags TypeTags]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def avgFlow[A: TypeTag: Numeric]: Flow[Seq[A], AveragedValue, NotUsed] = Flow[Seq[A]].map(avg[A])

  /** Flow to find average of sequence of BigDecimals
    *
    * @return AveragedValue
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def avgBDFlow: Flow[Seq[BigDecimal], AveragedValue, NotUsed] =
    Flow[Seq[BigDecimal]].map(avg[BigDecimal])

  /** Flow to find average of sequence of BigInts
    *
    * @return AveragedValue
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def avgBIFlow: Flow[Seq[BigInt], AveragedValue, NotUsed] = Flow[Seq[BigInt]].map(avg[BigInt])

  /** Flow to find average of sequence of Doubles
    *
    * @return AveragedValue
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def avgDFlow: Flow[Seq[Double], AveragedValue, NotUsed] = Flow[Seq[Double]].map(avg[Double])

  /** Flow to find average of sequence of Floats
    *
    * @return AveragedValue
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def avgFFlow: Flow[Seq[Float], AveragedValue, NotUsed] = Flow[Seq[Float]].map(avg[Float])

  /** Flow to find average of sequence of Ints
    *
    * @return AveragedValue
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def avgIFlow: Flow[Seq[Int], AveragedValue, NotUsed] = Flow[Seq[Int]].map(avg[Int])

  /** Flow to find average of sequence of Longs
    *
    * @return AveragedValue
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def avgLFlow: Flow[Seq[Long], AveragedValue, NotUsed] = Flow[Seq[Long]].map(avg[Long])

  /** Flow to sum sequences of AveragedValues to a single AveragedValue
    *
    * @return AveragedValue
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def sumAvgFlow: Flow[Seq[AveragedValue], AveragedValue, NotUsed] =
    Flow[Seq[AveragedValue]].map(sumAverageValues)

  /** Flow to estimate size of HLL
    *
    * @return estimated size of HLL
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL HLL]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def estSizeFlow: Flow[HLL, Double, NotUsed] = Flow[HLL].map(x => x.estimatedSize)

  /** Flow to map Hyper Log Log to Approximate[Long]
    *
    * @return Approximate[Long]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL HLL]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Approximate Approximate]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def toApproximate: Flow[HLL, Approximate[Long], NotUsed] = Flow[HLL].map(x => x.approximateSize)

  /** Flow to map sequence of HLL to sequence of Approximate[Long]
    *
    * @return Approximate[Long]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL HLL]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Approximate Approximate]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def toApproximates: Flow[Seq[HLL], Seq[Approximate[Long]], NotUsed] =
    Flow[Seq[HLL]].map(mapHLL2Approximate)

  /** Flow to sum sequence of HLL to an Approximate[Long]
    *
    * @return Approximate[Long]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL HLL]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Approximate Approximate]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def sumHLLs: Flow[Seq[HLL], Approximate[Long], NotUsed] = Flow[Seq[HLL]].map(sumHLLApproximateSizes)

  /** Flow to find max value in a sequence
    *
    * @tparam A: Ordering
    * @return max
    * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering Ordering]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def maxFlow[A: Ordering]: Flow[Seq[A], A, NotUsed] = Flow[Seq[A]].map(max[A])

  /** Flow to find min value in a sequence
    *
    * @tparam A: Ordering
    * @return min
    * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering Ordering]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def minFlow[A: Ordering]: Flow[Seq[A], A, NotUsed] = Flow[Seq[A]].map(min[A])

  /** Flow to find max value in a QTree
    *
    * @tparam A QTreeLike typeclass that construct QTree[A]
    * @return max
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[org.gs.algebird.typeclasses.QTreeLike]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def qTreeMaxFlow[A: QTreeLike]: Flow[QTree[A], Double, NotUsed] = Flow[QTree[A]].map(_.upperBound)

  /** Flow to find min value in a QTree
    *
    * @tparam A QTreeLike typeclass that construct QTree[A]
    * @return min
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[org.gs.algebird.typeclasses.QTreeLike]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def qTreeMinFlow[A: QTreeLike]: Flow[QTree[A], Double, NotUsed] = Flow[QTree[A]].map(_.lowerBound)

  /** Flow to find first quartile bounds in a QTree
    *
    * @tparam A QTreeLike typeclass that construct QTree[A]
    * @return lower, upper bounds
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[org.gs.algebird.typeclasses.QTreeLike]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def firstQuartileFlow[A: QTreeLike]: Flow[QTree[A], (Double, Double), NotUsed] = {
    Flow[QTree[A]].map(_.quantileBounds(0.25))
  }

  /** Flow to find second quartile bounds in a QTree
    *
    * @tparam A QTreeLike typeclass that construct QTree[A]
    * @return lower, upper bounds
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[org.gs.algebird.typeclasses.QTreeLike]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def secondQuartileFlow[A: QTreeLike]: Flow[QTree[A], (Double, Double), NotUsed] = {
    Flow[QTree[A]].map(_.quantileBounds(0.5))
  }

  /** Flow to find third quartile bounds in a QTree
    *
    * @tparam A QTreeLike typeclass that construct QTree[A]
    * @return lower, upper bounds
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[org.gs.algebird.typeclasses.QTreeLike]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def thirdQuartileFlow[A: QTreeLike]: Flow[QTree[A], (Double, Double), NotUsed] = {
    Flow[QTree[A]].map(_.quantileBounds(0.75))
  }

  /** Flow to find inter quartile mean in a QTree[BigDecimal]
    *
    * @return mean of middle 50th percentile
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def interQuartileMeanBDFlow: Flow[QTree[BigDecimal], (Double, Double), NotUsed] = {
    Flow[QTree[BigDecimal]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[BigInt]
    *
    * @return mean of middle 50th percentile
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def interQuartileMeanBIFlow: Flow[QTree[BigInt], (Double, Double), NotUsed] = {
    Flow[QTree[BigInt]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[Double]
    *
    * @return mean of middle 50th percentile
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def interQuartileMeanDFlow: Flow[QTree[Double], (Double, Double), NotUsed] = {
    Flow[QTree[Double]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[Float]
    *
    * @return mean of middle 50th percentile
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def interQuartileMeanFFlow: Flow[QTree[Float], (Double, Double), NotUsed] = {
    Flow[QTree[Float]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[Int]
    *
    * @return mean of middle 50th percentile
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def interQuartileMeanIFlow: Flow[QTree[Int], (Double, Double), NotUsed] = {
    Flow[QTree[Int]].map(_.interQuartileMean)
  }

  /** Flow to find inter quartile mean in a QTree[Long]
    *
    * @return mean of middle 50th percentile
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://doc.akka.io/api/akka/current/#akka.stream.scaladsl.Flow Flow]]
    */
  def interQuartileMeanLFlow: Flow[QTree[Long], (Double, Double), NotUsed] = {
    Flow[QTree[Long]].map(_.interQuartileMean)
  }
}
