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

import com.twitter.algebird.{Approximate, AveragedGroup, AveragedValue, Averager, BF, BloomFilter, CMS, CMSHasher,
  CMSMonoid, DecayedValue, DecayedValueMonoid, Functor, HLL, HyperLogLogAggregator, MaxAggregator, MinAggregator, QTree,
  QTreeSemigroup}
import com.github.garyaiki.dendrites.algebird.typeclasses.{HyperLogLogLike, QTreeLike}

/** Aggregation functions for Twitter Algebird.
  *
  * Algebird provides implicit implementations of common types which are imported here. Class extraction methods in
  * [[com.github.garyaiki.dendrites]] can be used to extract a field from case classes and tuples.
  *
  * == AveragedValue ==
  *
  * Average a Sequence of values `com.github.garyaiki.dendrites.algebird.AveragedSpec`
  * {{{
  * val bigDecimals: Seq[BigDecimal]
  * val avg0 = avg(bigDecimals)
  * }}}
  * Average a sequence of AveragedValues
  * {{{
  * val bigDecimals2: Seq[BigDecimal]
  * val avg1 = avg(bigDecimals2)
  * val avgs = Vector[AveragedValue](avg0, avg1)
  * val avgSum = sumAverageValues(avgs)
  * }}}
  *
  * == BloomFilter ==
  * Fast find if a word is in a dictionary
  * OSX and Linux have dictionaries you can use to create BloomFilters
  * `com.github.garyaiki.dendrites.fixtures.SysProcessUtils` for Paths to properNames, connectives, words and functions
  * to read them `com.github.garyaiki.dendrites.algebird.fixtures.BloomFilterBuilder`
  * for creation of BloomFilters for these dictionaries and select test words for them.
  *
  * Create a BloomFilter for OSX words dictionary
  * `com.github.garyaiki.dendrites.algebird.BloomFilterSpec`
  * {{{
  * val falsePositivepProb: Double = 0.01
  * val words = readWords(wordsPath)
  * val wordsBF = createBF(words, fpProb)
  * }}}
  * Is word in BloomFilter
  * {{{
  * val falsePositivepProb: Double = 0.01
  * val word = "path"
  * val inDict = wordsBF.contains(word).isTrue
  * }}}
  * Is false positive rate acceptable
  * {{{
  * val falsePositivepProb: Double = 0.01
  * val wordsFalseWords: IndexedSeq[String]
  * val falsePositives = for {
  * i <- wordsFalseWords
  * if wordsBF.contains(i).isTrue
  * } yield i
  * val acceptable = falsePositives.size < words.size * fpProb
  * }}}
  * == CountMinSketch ==
  *
  * Test data is IP addresses repeated a random number of times
  * `com.github.garyaiki.dendrites.algebird.CountMinSketchSpec`
  * Estimate total number of elements seen so far `com.github.garyaiki.dendrites.fixtures.InetAddressesBuilder`
  * {{{
  * val addrs = inetAddresses(ipRange)
  * val longZips = inetToLongZip(addrs)
  * val longs = testLongs(longZips)
  * implicit val m = createCMSMonoid[Long]()
  * val cms = createCountMinSketch(longs)
  * val estimatedCount = cms.totalCount
  * }}}
  *
  * Estimate count of elements with the same value as the one selected
  * {{{
  * val estFreq = cms.frequency(longZips(5))
  * }}}
  *
  * Sum a Sequence of CountMinSketch then estimate combined total number of elements
  * {{{
  * val cms1 = createCountMinSketch(longs)
  * val cmss = Vector(cms, cms1)
  * val cmsSum = sumCountMinSketch(cmss)
  * val estimatedCount = cmsSum.totalCount
  * }}}
  *
  * From a Sequence of CountMinSketch estimate count of elements with the indexed same value
  * {{{
  * val estFreq = cmsSum.frequency(longZips(5))
  * }}}
  *
  * == DecayedValue ==
  *
  * Test data is a sine wave with a value for each of 360 degrees with a corresponding time value. The idea is a
  * rising and falling value over a year `com.github.garyaiki.dendrites.fixtures.TrigUtils`
  *
  * Moving average from the initial value to specified index `com.github.garyaiki.dendrites.algebird.DecayedValueSpec`
  * {{{
  * val sines = genSineWave(100, 0 to 360)
  * val days = Range.Double(0.0, 361.0, 1.0)
  * val sinesZip = sines.zip(days)
  * val decayedValues = toDecayedValues(sinesZip, 10.0, None)
  * val avgAt90 = decayedValues(90).average(10.0)
  * }}}
  *
  * Moving average from specified index to specified index
  * {{{
  * val avg80to90 = decayedValues(90).averageFrom(10.0, 80.0, 90.0)
  * }}}
  *
  * == HyperLogLog ==
  *
  * Create a HLL from a sequence of Int `com.github.garyaiki.dendrites.algebird.HyperLogLogSpec`
  * {{{
  * implicit val ag = HyperLogLogAggregator(12)
  * val ints: Seq[Int]
  * val hll = createHLL(ints)
  * }}}
  * Create a sequence of HLL
  * {{{
  * val ints2: Seq[Int]
  * val hll2 = createHLL(ints2)
  * val hlls = Vector(hll, hll2)
  * }}}
  * Create a HLL from a sequence of Long
  * {{{
  * val longs: Seq[Long]
  * val hll = createHLL(longs)
  * }}}
  * Create a sequence of HLL
  * {{{
  * val longs2: Seq[Long]
  * val hll2 = createHLL(longs2)
  * val hlls = Vector(hll, hll2)
  * }}}
  * Sum a Sequence of HLL and estimate total size
  * {{{
  * val sum = hlls.reduce(_ + _)
  * val size = sum.estimatedSize
  * }}}
  * Create a sequence of Approximate HHL approximate
  * Map a sequence of HLL to a sequence of Approximate
  * {{{
  * val hlls = Vector(hll, hll2)
  * val approxs = mapHLL2Approximate(hlls)
  * }}}
  * Sum a Sequence of Approximate and estimate total size
  * {{{
  * val sum = approxs.reduce(_ + _)
  * }}}
  *
  * == QTree ==
  *
  * Build QTree from a Sequence `com.github.garyaiki.dendrites.algebird.fixtures.QTreeBuilder`
  * {{{
  * val level = 5
  * implicit val qtBDSemigroup = new QTreeSemigroup[BigDecimal](level)
  * val qtBD = buildQTree[BigDecimal](bigDecimals)
  * }}}
  * Get its InterQuartileMean `com.github.garyaiki.dendrites.algebird.QTreeSpec`
  * {{{
  * val iqm = qtBD.interQuartileMean
  * // iqm._1 lower bound
  * // iqm._2 upper bound
  * }}}
  * Sum a Sequence of QTrees to a QTree
  * {{{
  * val qTrees = Vector(qtBD, qtBD2)
  * val sumQTree = sumQTrees(qTrees)
  * }}}
  * == Functor ==
  *
  * Map elements of a sequence.
  * {{{
  * def wrapMax[Int](x: Int) = Max(x)
  * val wm = SeqFunctor.map[Int, Max[Int]](List(1,2,3,4))(wrapMax)
  *
  * val bigDecimals: Seq[BigDecimal]
  * val negBigDecimals = SeqFunctor.map[BigDecimal, BigDecimal](bigDecimals)(negate)
  * val invertedBigDecimals = SeqFunctor.map[BigDecimal, BigDecimal](bigDecimals)(inverse)
  * }}}
  *
  * Map the mapped elements of a sequence: f() andThen g().
  * {{{
  * val bigDecimals: Seq[BigDecimal]
  * val invertedNegBigDecimals = andThen[BigDecimal, BigDecimal, BigDecimal](ap)( inverse)( negate)
  * }}}
  *
  * == Max Min ==
  * For Sequence types that have a Semigroup, Monoid and Ordering
  * Get Max element of a sequence. `com.github.garyaiki.dendrites.algebird.MaxSpec`
  * {{{
  * val iqm = qtBD.interQuartileMean
  *
  * val bigDecimals: Seq[BigDecimal]
  * val max = max(bigDecimals)
  * val optBigDecs: [Option[BigDecimal]]
  * val max2 = max(optBigDecs.flatten)
  * val eithBigInts = Seq[Either[String, BigInt]]
  * val max3 = max(filterRight(eithBigInts)
  * }}}
  *
  * Get Min element of a sequence. `com.github.garyaiki.dendrites.algebird.MinSpec`
  * {{{
  * val bigDecimals: Seq[BigDecimal]
  * val min = min(bigDecimals)
  * val optBigDecs: [Option[BigDecimal]]
  * val min2 = min(optBigDecs.flatten)
  * val eithBigInts = Seq[Either[String, BigInt]]
  * val min3 = min(filterRight(eithBigInts)
  * }}}
  *
  * @author Gary Struthers
  */
package object algebird {

  /** MaxAggregator finds maximum element in a Seq
    *
    * @tparam A: Ordering element type, uses Ordering[A]
    * @param xs Seq[A]
    * @return max
    *
    * @see [[http://twitter.github.io/algebird/datatypes/min_and_max.html MaxAggregator]]
    * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering Ordering]]
    */
  def max[A: Ordering](xs: Seq[A]): A = MaxAggregator[A].reduce(xs)

  /** MinAggregator find minimum element in a Seq
    *
    * @tparam A: Ordering element type, uses Ordering[A]
    * @param xs Seq[A]
    * @return min element
    *
    * @see [[http://twitter.github.io/algebird/datatypes/min_and_max.html MinAggregator]]
    * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering Ordering]]
    */
  def min[A: Ordering](xs: Seq[A]): A = MinAggregator[A].reduce(xs)

  /** Functor maps sequence[A] to sequence[B]
    *
    * @tparam A original element
    * @tparam B returned element
    * @param fa Seq[A]
    * @param f A => B
    * @return Seq[B]
    */
  implicit object SeqFunctor extends Functor[Seq] {
    def map[A, B](fa: Seq[A])(f: A => B): Seq[B] = (for { a <- fa } yield f(a))
  }

  /** Functor composes 2 functors map Seq[A] -> Seq[B] -> Seq[C]
    *
    * @tparam A original element
    * @tparam B intermediate element
    * @tparam C returned element
    * @param fa Seq[A]
    * @param f A => B
    * @param g B => C
    * @param ev implicit Functor[Seq]
    * @return Seq[C]
    */
  def andThen[A, B, C](fa: Seq[A])(f: A => B)(g: B => C)(implicit ev: Functor[Seq]): Seq[C] = ev.map(ev.map(fa)(f))(g)

  /** AverageValue of a Seq of Numeric elements. Create AveragedValue for elements then sum
    *
    * @tparam A
    * @param xs Seq, elements must be Numeric
    * @return AverageValue
    *
    * @note when there's more than 1 Numeric type in scope you must use explicitly typed avg below
    * @see [[http://twitter.github.io/algebird/datatypes/averaged_value.html AveragedValue]]
    */
  def avg[A: Numeric](xs: Seq[A]): AveragedValue = {
    val at = andThen[A, Double, AveragedValue](xs)(implicitly[Numeric[A]].toDouble)(Averager.prepare(_))
    at.reduce(AveragedGroup.plus(_, _))
  }

  /** AverageValue of a Seq of AverageValues.
    *
    * @param xs Seq[AverageValue]
    * @return AverageValue
    */
  def sumAverageValues(xs: Seq[AveragedValue]): AveragedValue = xs.reduce(AveragedGroup.plus(_, _))

  /** Create BloomFilter configure and load it from a Seq of words
    *
    * @param words
    * @param fpProb false positive probability, 1% default
    * @return BloomFilter
    *
    * @see [[http://twitter.github.io/algebird/datatypes/approx/bloom_filter.html BloomFilter]]
    */
  def createBF(words: Seq[String], fpProb: Double = 0.01): BF[String] = {
    val wc = words.size
    val bfMonoid = BloomFilter[String](wc, fpProb)
    bfMonoid.create(words: _*)
  }

  /** Find if strings in Bloom filter
    *
    * @param xs strings to test
    * @param bf configured and data initialized Bloom filter
    * @return tuple ._1 is matches including false positives, ._2 are not in BF
    */
  def bloomFilterPartition(xs: Seq[String])(implicit bf: BF[String]): (Seq[String], Seq[String]) =
    xs.partition(bf.contains(_).isTrue)

  /** Array[Byte] hasher for CMS
    *
    * @see [[http://twitter.github.io/algebird/datatypes/approx/countminsketch.html CountMinSketch]]
    */
  implicit object CMSHasherArrayByte extends CMSHasher[Array[Byte]] {
    override def hash(a: Int, b: Int, width: Int)(x: Array[Byte]): Int = {
      val hash: Int = scala.util.hashing.MurmurHash3.arrayHash(x, a)
      val positiveHash = hash & Int.MaxValue
      positiveHash % width
    }
  }

  /** map Double to Array[Byte]
    *
    * @param d Double
    * @return Array[Byte]
    */
  def doubleToArrayBytes(d: Double): Array[Byte] = {
    val l: Long = java.lang.Double.doubleToLongBits(d)
    java.nio.ByteBuffer.allocate(8).putLong(l).array()
  }

  implicit val cmsHasherDouble: CMSHasher[Double] = CMSHasherArrayByte.contramap((d: Double) => doubleToArrayBytes(d))

  def floatToArrayBytes(f: Float): Array[Byte] = doubleToArrayBytes(f.toDouble)

  implicit val cmsHasherFloat: CMSHasher[Float] = CMSHasherArrayByte.contramap((f: Float) => floatToArrayBytes(f))

  /** Double.NEGATIVE_INFINITY or Double.POSITIVE_INFINITY when > Double */
  def bigDecimalToArrayBytes(bd: BigDecimal): Array[Byte] = doubleToArrayBytes(bd.toDouble)

  implicit val cmsHasherBigDecimal: CMSHasher[BigDecimal] =
    CMSHasherArrayByte.contramap((bd: BigDecimal) => bigDecimalToArrayBytes(bd))

  /** create CMSMonoid
    *
    * @tparam K elements which are implicitly Ordering[K] and CMSHasher[K]
    * @param eps
    * @param delta
    * @param seed
    * @return CMSMonoid[K]
    */
  def createCMSMonoid[K: Ordering: CMSHasher](eps: Double = 0.001, delta: Double = 1E-10, seed: Int = 1): CMSMonoid[K] =
    new CMSMonoid[K](eps, delta, seed)

  /** Create a CMS
    *
    * @tparam K elements which are implicitly Ordering[K] and CMSHasher[K]
    * @param xs data
    * @param m implicit CMSMonoid for K
    * @return CMS for data
    */
  def createCountMinSketch[K: Ordering: CMSHasher](xs: Seq[K])(implicit m: CMSMonoid[K]): CMS[K] = m.create(xs)

  /** Sum a Sequence of CMS
    *
    * @tparam K elements which are implicitly Ordering[K] and CMSHasher[K]
    * @param xs Sequence of CMS
    * @param m CMSMonoid
    * @return CMS as the sum of Sequence of CMS
    */
  def sumCountMinSketch[K: Ordering: CMSHasher](xs: Seq[CMS[K]])(implicit m: CMSMonoid[K]): CMS[K] =
    xs.reduce(m.plus(_, _))

  /** Turn a sequence of value, time tuples into a seq of DecayedValues
    *
    * @param halfLife to scale value based on time
    * @param last is initial element, if None use implicit monoid.zero
    * @param xs sequence of value, time tuples
    * @param m implicit DecayedValueMonoid used to scan from initial value
    * @return seq of DecayedValues
    *
    * @see [[http://twitter.github.io/algebird/datatypes/decayed_value.html DecayedValue]]
    */
  def toDecayedValues(halfLife: Double, last: Option[DecayedValue] = None)(xs: Seq[(Double, Double)])
    (implicit m: DecayedValueMonoid): Seq[DecayedValue] = {
    val z = last match {
      case None    => m.zero
      case Some(x) => x
    }

    def op(previous: DecayedValue, x: (Double, Double)): DecayedValue = {
      val (value, time) = x
      val d = time match {
        case _ if (time < 1.0)      => 1.0
        case _ if (time < halfLife) => time
        case _                      => halfLife
      }
      m.plus(previous, DecayedValue.build(value, time, d))
    }
    xs.scanLeft(z)(op)
  }

  /** Create HyperLogLog
    *
    * @tparam A: can only be Int, Long
    * @param xs sequence
    * @param ev implicit HyperLogLogLike typeclass to create HLL
    * @param agg implicit HyperLogLogAggregator, initialized with # of bits for hashing
    * @return an HLL
    *
    * @see [[http://twitter.github.io/algebird/datatypes/approx/hyperloglog.html HyperLogLog]]
    * @see [[com.github.garyaiki.dendrites.algebird.typeclasses.HyperLogLogLike]]
    */
  def createHLL[A: HyperLogLogLike](xs: Seq[A])(implicit agg: HyperLogLogAggregator): HLL =
    implicitly[HyperLogLogLike[A]].apply(xs)

  /** Map Sequence of HyperLogLogs to Sequence of Approximate
    *
    * @param xs sequence of HLL
    * @return Sequence of Approximate
    *
    * @see [[http://twitter.github.io/algebird/datatypes/approx/approximate.html Approximate]]
    */
  def mapHLL2Approximate(xs: Seq[HLL]): Seq[Approximate[Long]] = xs.map(_.approximateSize)

  /** Estimate total count of distinct integer values in multiple HyperLogLogs
    *
    * @param xs sequence of HLL
    * @return estimate count in an Approximate object
    */
  def sumHLLApproximateSizes(xs: Seq[HLL]): Approximate[Long] = xs.reduce(_ + _).approximateSize

  def buildQTrees[A: QTreeLike](vals: Seq[A]): Seq[QTree[A]] = vals.map(implicitly[QTreeLike[A]].apply(_))

  /** Build a QTree from a Seq
    *
    * @tparam A: one of: BigDecimal, BigInt, Double, Float, Int, Long
    * @param vals
    * @param ev implicit Typeclass to build from Seq
    * @param sg implicit QTreeSemigroup
    * @return
    *
    * @see [[http://twitter.github.io/algebird/datatypes/approx/q_tree.html QTree]]
    * @see [[com.github.garyaiki.dendrites.algebird.typeclasses.QTreeLike]]
    */
  def buildQTree[A: QTreeLike](vals: Seq[A])(implicit sg: QTreeSemigroup[A]): QTree[A] =
    vals.map(implicitly[QTreeLike[A]].apply(_)).reduce(sg.plus(_, _))

  /** Sum a Sequence of QTrees
    *
    * @tparam A: BigDecimal, BigInt, Double, Float, Int, Long
    * @param qTrees Seq[QTree[A]]
    * @param sg implicit QTreeSemigroup
    * @return QTree[A]
    */
  def sumQTrees[A: QTreeSemigroup](qTrees: Seq[QTree[A]]): QTree[A] =
    qTrees.reduce(implicitly[QTreeSemigroup[A]].plus(_, _))
}
