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
package org.gs

import com.twitter.algebird.{Approximate, AveragedValue, AveragedGroup, Averager, BF, BloomFilter,
  CMS, CMSHasher, CMSMonoid, DecayedValue, DecayedValueMonoid, Field, Functor, Group, HLL,
  HyperLogLogAggregator, IntRing, LongRing, MaxAggregator, MinAggregator, Monoid, NumericRing,
  QTree, QTreeSemigroup, Ring, Semigroup}
import org.gs.algebird.typeclasses.{HyperLogLogLike, QTreeLike}

/** Aggregation functions for Twitter Algebird.
  *
  * Algebird provides implicit implementations of common types which are imported here. Class
  * extraction methods in [[org.gs]] can be used to extract a field from your case classes and tuples.
  * When the extracted field is an already supported type, you don't have to write custom Algebird
  * classes.
  *
  * == AveragedValue ==
  *
  * Average a Sequence of values `org.gs.algebird.AveragedSpec`
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
  * OSX has dictionaries you can use to create BloomFilters
  * `org.gs.fixtures.SysProcessUtils` for Paths to properNames, connectives, and words and
  * functions to read their words
  * `org.gs.algebird.fixtures.BloomFilterBuilder` for creation of BloomFilters for these
  * dictionaries and select test words for them.
  *
  * Create a BloomFilter for OSX words dictionary `org.gs.algebird.BloomFilterSpec`
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
  * Test data is IP addresses repeated a random number of times `org.gs.algebird.CountMinSketchSpec`
  * Estimate total number of elements seen so far `org.gs.fixtures.InetAddressesBuilder`
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
  * Test data is a sine wave with a value for each of 360 degrees with a corresponding time value
  * The idea is a rising and falling value over a year `org.gs.fixtures.TrigUtils`
  *
  * Moving average from the initial value to specified index `org.gs.algebird.DecayedValueSpec`
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
  * Create a HLL from a sequence of Int `org.gs.algebird.HyperLogLogSpec`
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
  * Build QTree from a Sequence `org.gs.algebird.fixtures.QTreeBuilder`
  * {{{
  * val level = 5
  * implicit val qtBDSemigroup = new QTreeSemigroup[BigDecimal](level)
  * val qtBD = buildQTree[BigDecimal](bigDecimals)
  * }}}
  * Get its InterQuartileMean `org.gs.algebird.QTreeSpec`
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
  * Get Max element of a sequence. `org.gs.algebird.MaxSpec`
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
  * Get Min element of a sequence. `org.gs.algebird.MinSpec`
  * {{{
  * val bigDecimals: Seq[BigDecimal]
  * val min = min(bigDecimals)
  * val optBigDecs: [Option[BigDecimal]]
  * val min2 = min(optBigDecs.flatten)
  * val eithBigInts = Seq[Either[String, BigInt]]
  * val min3 = min(filterRight(eithBigInts)
  * }}}
  *
  * == Semigroup ==
  * Plus function obeys associative law `org.gs.algebird.SemigroupSpec`
  * Sum elements of a sequence that may be empty.
  * {{{
  * val bigDecimals: Seq[BigDecimal]
  * val opt = sumOption(bigDecimals)
  * val sum = opt.get
  * val eithBigInts = Seq[Either[String, BigInt]]
  * val eith = sumOption(eithBigInts).get
  * val sum2 = eith.right.get
  * }}}
  *
  * == Monoid ==
  * Extends Semigroup with a zero element `org.gs.algebird.MonoidSpec`
  * Sum sequence elements for a type that has a zero under addition.
  * {{{
  * val doubles: Seq[Double]
  * val sum = sum(doubles)
  * val optBigInts: Seq[Option[BigInt]]
  * val sum2 = sum(optBigInts.flatten)
  * val eithFloats = Seq[Either[String, Float]]
  * val sum3 = sum(eithFloats).right.get
  * }}}
  *
  * == Group ==
  * Extends Monoid with minus, negate operators `org.gs.algebird.GroupSpec`
  * Negate a value
  * {{{
  * val float = 3131.7f
  * val neg = negate(float)
  * val double = 3130.0
  * val neg2 = negate(double.get)
  * val int = 2847
  * val neg3 = negate(int.right.get)
  * }}}
  *
  * Subract a value from another
  * {{{
  * val float = 3131.7f
  * val diff = minus(float, 1.7f)
  * val opt = Some(3130.0)
  * val diff2 = minus(opt.get, 30.0)
  * val eithLong = 2847L
  * val diff3 = minus(eithLong.right.get, 47L)
  * }}}
  *
  * == Ring ==
  * Extends Group with times, product and one identity under multiplication
  * Multiply a value by another `org.gs.algebird.RingSpec`
  * {{{
  * val float = 3131.7f
  * val prod = times(float, 1.7f)
  * val opt = Some(3130.0)
  * val prod2 = times(opt.get, 30.0)
  * val eithLong = 2847L
  * val prod3 = times(eithLong.right.get, 47L)
  * }}}
  *
  * Multiply elements of a sequence `(((xs[0] * xs[1]) * xs[2]) * xs[3])` for a type with identity.
  * {{{
  * val doubles: Seq[Double]
  * val prod = product(doubles)
  * val optBigInts: Seq[Option[BigInt]]
  * val prod2 = product(optBigInts.flatten)
  * val eithFloats = Seq[Either[String, Float]]
  * val prod3 = product(eithFloats).right.get
  * }}}
  *
  * == Field ==
  * Extends Ring with inverse, div  `org.gs.algebird.FieldSpec`
  * Invert a value a -> 1/a
  * {{{
  * val float = 3131.7f
  * val recip = inverse(float)
  * }}}
  * {{{
  * val optDouble = Some(3130.0)
  * val recip = inverse(double.get)
  * }}}
  * {{{
  * val eithInt = Right(2847)
  * val recip = inverse(eithInt.right.get)
  * }}}
  *
  * Divide a value
  * {{{
  * val float = 3131.7f
  * val quotient = div(float, 1.7f)
  * }}}
  * {{{
  * val opt = Some(3130.0)
  * val quotient = div(opt.get, 30.0)
  * }}}
  * {{{
  * val eithLong = 2847L
  * val quotient = div(eithLong.right.get, 47L)
  * }}}
  * @author Gary Struthers
  */
package object algebird {

  /** Semigroup sums sequence elements
    *
    * @tparam A: Semigroup, element
    * @param xs sequence
    * @return Some(sum) or None if xs empty
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Semigroup "Semigroup"]]
    */
  def sumOption[A: Semigroup](xs: Seq[A]): Option[A] = implicitly[Semigroup[A]].sumOption(xs)

  /** Monoid sums sequence elements
    *
    * @tparam A: Monoid, element
    * @param xs sequence
    * @return sum or Monoid[A] zero for empty sequence
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Monoid "Monoid"]]
    *
    */
  def sum[A: Monoid](xs: Seq[A]): A = implicitly[Monoid[A]].sum(xs)

  /** Group negates an element
    *
    * @tparam A: Group, element
    * @param x element to negate
    * @return -x
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Group Group]]
    */
  def negate[A: Group](x: A): A = implicitly[Group[A]].negate(x)

  /** Group subtracts an element from another
    *
    *
    * @tparam A: Group, element
    * @param l
    * @param r
    * @return l - r
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Group Group]]
    */
  def minus[A: Group](l: A, r: A): A = implicitly[Group[A]].minus(l, r)

  /** Ring multiplies 2 elements
    *
    * @tparam A: Ring, element
    * @param l
    * @param r
    * @return l * r
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Ring Ring]]
    */
  def times[A: Ring](l: A, r: A): A = implicitly[Ring[A]].times(l, r)

  /** Ring multiplies sequence elements
    *
    * @tparam A: Ring, element
    * @param xs sequence
    * @return sum or Monoid[A] zero for empty sequence
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Ring Ring]]
    */
  def product[A: Ring](xs: Seq[A]): A = implicitly[Ring[A]].product(xs)

  /** Field inverts an element
    *
    * @tparam A: element and Field
    * @param x element to divide one
    * @return 1/x
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Field Field]]
    */
  def inverse[A: Field](x: A): A = implicitly[Field[A]].inverse(x)

  /** Field divides an element by another
    *
    * @tparam A: element and Field
    * @param l
    * @param r
    * @return l * r
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Field Field]]
    */
  def div[A: Field](l: A, r: A): A = implicitly[Field[A]].div(l, r)

  /** MaxAggregator finds maximum element in a Seq
    *
    * @tparam A: Ordering element type that can be ordered, uses Ordering[A]
    * @param xs Seq[A]
    * @return max
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.MaxAggregator MaxAggregator]]
    * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering Ordering]]
    */
  def max[A: Ordering](xs: Seq[A]): A = MaxAggregator[A].reduce(xs)

  /** MinAggregator find minimum element in a Seq
    *
    * @tparam A: Ordering element type that can be ordered, uses Ordering[A]
    * @param xs Seq[A]
    * @return min element
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.MinAggregator MinAggregator]]
    * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering Ordering]]
    */
  def min[A: Ordering](xs: Seq[A]): A = MinAggregator[A].reduce(xs)

  /** Field[BigDecimal] implicit
    *
    * BigDecimal implicits supported in Algebird with NumericRing[BigDecimal]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Field Field]]
    */
  implicit object BigDecimalField extends NumericRing[BigDecimal] with Field[BigDecimal] {
    override def inverse(v: BigDecimal): BigDecimal = {
      assertNotZero(v)
      1 / v
    }
  }

  /** Field[BigInt] implicit
    *
    * BigInt implicits supported in Algebird with NumericRing[BigInt]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Field Field]]
    */
  implicit object BigIntField extends NumericRing[BigInt] with Field[BigInt] {
    override def div(l: BigInt, r: BigInt): BigInt = {
      assertNotZero(r)
      l / r
    }

    override def inverse(v: BigInt): BigInt = {
      assertNotZero(v)
      1 / v
    }
  }

  /** Field[Int] implicit
    *
    * Int implicits supported in Algebird with IntRing
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Field Field]]
    */
  implicit object IntField extends Field[Int] {
    def zero: Int = IntRing.zero

    def one: Int = IntRing.one

    override def negate(v: Int): Int = IntRing.negate(v)

    def plus(l: Int, r: Int): Int = IntRing.plus(l, r)

    override def minus(l: Int, r: Int): Int = IntRing.minus(l, r)

    def times(l: Int, r: Int): Int = IntRing.times(l, r)

    override def div(l: Int, r: Int): Int = {
      assertNotZero(r)
      l / r
    }

    override def inverse(v: Int): Int = {
      assertNotZero(v)
      1 / v
    }
  }

  /** Field[Long] implicit
    *
    * Long implicits supported in Algebird with LongRing
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Field Field]]
    */
  implicit object LongField extends Field[Long] {
    def zero: Long = LongRing.zero

    def one: Long = LongRing.one

    override def negate(v: Long): Long = LongRing.negate(v)

    def plus(l: Long, r: Long): Long = LongRing.plus(l, r)

    override def minus(l: Long, r: Long): Long = LongRing.minus(l, r)

    def times(l: Long, r: Long): Long = LongRing.times(l, r)

    override def div(l: Long, r: Long): Long = {
      assertNotZero(r)
      l / r
    }

    override def inverse(v: Long): Long = {
      assertNotZero(v)
      1 / v
    }
  }

  /** Functor maps sequence[A] to sequence[B]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Functor Functor]]
    *
    * @tparam A original element
    * @tparam B returned element
    * @param fa Seq[A]
    * @param f A => B
    * @param ev implicit Functor[Seq]
    * @return Seq[B]
    */
  implicit object SeqFunctor extends Functor[Seq] {
    def map[A, B](fa: Seq[A])(f: A => B): Seq[B] = (for {a <- fa} yield f(a))
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
  def andThen[A, B, C](fa: Seq[A])(f: A => B)(g: B => C)(implicit ev: Functor[Seq]): Seq[C] = {
    ev.map(ev.map(fa)(f))(g)
  }

  /** AverageValue of a Seq of Numeric elements. Create AveragedValue for each element then sum
    *
    * @tparam A: Numeric, elements must be Numeric
    * @param xs Seq
    * @param evidence implicit Numeric[A]
    * @return AverageValue
    *
    * @note when there's more than 1 Numeric type in scope you must use explicitly typed avg below
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedGroup$ AveragedGroup]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Averager$ Averager]]
    */
  def avg[A: Numeric](xs: Seq[A]): AveragedValue = {
    val at = andThen[A, Double, AveragedValue](xs)(implicitly[Numeric[A]].toDouble)(
      Averager.prepare(_))
    at.reduce(AveragedGroup.plus(_, _))
  }

  /** AverageValue of a Seq of AverageValues.
    *
    * @param xs Seq
    * @return AverageValue
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedGroup$ AveragedGroup]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue AveragedValue]]
    */
  def sumAverageValues(xs: Seq[AveragedValue]): AveragedValue = {
    xs.reduce(AveragedGroup.plus(_, _))
  }

  /** Create BloomFilter configure and load it from a Seq of words
    *
    * @param words
    * @param fpProb false positive probability, 1% default
    * @return BloomFilter
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.BloomFilter$ BloomFilter]]
    */
  def createBF(words: Seq[String], fpProb: Double = 0.01): BF = {
    val wc = words.size
    val bfMonoid = BloomFilter(wc, fpProb)
    bfMonoid.create(words: _*)
  }

  /** Find if strings in Bloom filter
    *
    * @param xs strings to test
    * @param bf configured and data initialized Bloom filter
    * @return tuple ._1 is matches including false positives, ._2 are not in BF
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.BloomFilter$ BloomFilter]]
    */
  def bloomFilterPartition(xs: Seq[String])(implicit bf: BF): (Seq[String], Seq[String]) =
    xs.partition(bf.contains(_).isTrue)

  /** Array[Byte] hasher for CMS
    *
    * @see [[https://twitter.github.io/algebird/index.html#com.twitter.algebird.CMSHasher CMSHasher]]
    */
  implicit object CMSHasherArrayByte extends CMSHasher[Array[Byte]] {
    override def hash(a: Int, b: Int, width: Int)(x: Array[Byte]): Int = {
      val hash: Int = scala.util.hashing.MurmurHash3.arrayHash(x, a)
      val positiveHash = hash & Int.MaxValue
      positiveHash % width
    }
  }

  def doubleToArrayBytes(d: Double): Array[Byte] = {
    val l: Long = java.lang.Double.doubleToLongBits(d)
    java.nio.ByteBuffer.allocate(8).putLong(l).array()
  }

  implicit val cmsHasherDouble: CMSHasher[Double] =
    CMSHasherArrayByte.contramap((d: Double) => doubleToArrayBytes(d))

  def floatToArrayBytes(f: Float): Array[Byte] = doubleToArrayBytes(f.toDouble)
  implicit val cmsHasherFloat: CMSHasher[Float] =
    CMSHasherArrayByte.contramap((f: Float) => floatToArrayBytes(f))

  /** Double.NEGATIVE_INFINITY or Double.POSITIVE_INFINITY when > Double */
  def bigDecimalToArrayBytes(bd: BigDecimal): Array[Byte] =
    doubleToArrayBytes(bd.toDouble)

  implicit val cmsHasherBigDecimal: CMSHasher[BigDecimal] =
    CMSHasherArrayByte.contramap((bd: BigDecimal) => bigDecimalToArrayBytes(bd))

  /** create CMSMonoid
    *
    * @tparam K elements which are implicitly Ordering[K] and CMSHasher[K]
    * @param eps
    * @param delta
    * @param seed
    * @return CMSMonoid[K]
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.CMSMonoid CMSMonoid]]
    * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering Ordering]]
    */
  def createCMSMonoid[K: Ordering: CMSHasher](
          eps: Double = 0.001, delta: Double = 1E-10, seed: Int = 1): CMSMonoid[K] =
                                                new CMSMonoid[K](eps, delta, seed)

  /** Create a CMS
    *
    * @tparam K elements which are implicitly Ordering[K] and CMSHasher[K]
    * @param xs data
    * @param monoid implicit CMSMonoid for K
    * @return CMS for data
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.CMSHasher CMSHasher]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.CMSMonoid CMSMonoid]]
    * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering Ordering]]
    */
  def createCountMinSketch[K: Ordering: CMSHasher](xs: Seq[K])(implicit monoid: CMSMonoid[K]):
      CMS[K] = monoid.create(xs)

  /** Sum a Sequence of CMS
    *
    * @tparam K elements which are implicitly Ordering[K] and CMSHasher[K]
    * @param xs Sequence of CMS
    * @param monoid
    * @return CMS as the sum of Sequence of CMS
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.CMSHasher CMSHasher]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.CMSMonoid CMSMonoid]]
    * @see [[http://www.scala-lang.org/api/current/index.html#scala.math.Ordering Ordering]]
    */
  def sumCountMinSketch[K: Ordering: CMSHasher](xs: Seq[CMS[K]])(implicit monoid: CMSMonoid[K]):
      CMS[K] = xs.reduce(monoid.plus(_, _))

  /** Turn a sequence of value, time tuples into a seq of DecayedValues
    *
    * @param xs sequence of value, time tuples
    * @param halfLife to scale value based on time
    * @param last is initial element, if None use implicit monoid.zero
    * @param monoid implicit DecayedValueMonoid used to scan from initial value
    * @return seq of DecayedValues
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.DecayedValue DecayedValue]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.DecayedValueMonoid DecayedValueMonoid]]
    */
  def toDecayedValues(halfLife: Double, last: Option[DecayedValue] = None)
      (xs: Seq[(Double, Double)])(implicit monoid: DecayedValueMonoid): Seq[DecayedValue] = {
    val z = last match {
      case None    => monoid.zero
      case Some(x) => x
    }

    def op(previous: DecayedValue, x: (Double, Double)): DecayedValue = {
      val (value, time) = x
      val d = time match {
        case _ if (time < 1.0)      => 1.0
        case _ if (time < halfLife) => time
        case _                      => halfLife
      }
      monoid.plus(previous, DecayedValue.build(value, time, d))
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
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL HLL]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HyperLogLogAggregator HyperLogLogAggregator]]
    * @see [[org.gs.algebird.typeclasses.HyperLogLogLike]]
    */
  def createHLL[A: HyperLogLogLike](xs: Seq[A])(
    implicit ev: HyperLogLogLike[A], agg: HyperLogLogAggregator): HLL = ev(xs)

  /** Map Sequence of HyperLogLogs to Sequence of Approximate
    *
    * @param xs sequence of HLL
    * @return Sequence of Approximate
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL HLL]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Approximate Approximate]]
    */
  def mapHLL2Approximate(xs: Seq[HLL]): Seq[Approximate[Long]] = xs.map(_.approximateSize)

  /** Estimate total count of distinct integer values in multiple HyperLogLogs
    *
    * @param xs sequence of HLL
    * @return estimate count in an Approximate object
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL HLL]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.Approximate Approximate]]
    */
  def sumHLLApproximateSizes(xs: Seq[HLL]): Approximate[Long] = {
    xs.reduce(_ + _).approximateSize
  }

  def buildQTrees[A: QTreeLike](vals: Seq[A])(implicit ev: QTreeLike[A]): Seq[QTree[A]] =
    vals.map(ev(_))

  /** Build a QTree from a Seq
    *
    * @tparam A: one of: BigDecimal, BigInt, Double, Float, Int, Long
    * @param vals
    * @param ev implicit Typeclass to build from Seq
    * @param sg implicit QTreeSemigroup
    * @return
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTreeSemigroup QTreeSemigroup]]
    * @see [[org.gs.algebird.typeclasses.QTreeLike]]
    */
  def buildQTree[A: QTreeLike](vals: Seq[A])(implicit ev: QTreeLike[A], sg: QTreeSemigroup[A]):
      QTree[A] = vals.map { ev(_) }.reduce { sg.plus(_, _) }

  /** Sum a Sequence of QTrees
    *
    * @tparam A: BigDecimal, BigInt, Double, Float, Int, Long
    * @param vals
    * @param ev Typeclass to build from Seq
    * @param sg
    * @return
    *
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTree QTree]]
    * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.QTreeSemigroup QTreeSemigroup]]
    */
  def sumQTrees[A: QTreeSemigroup](qTrees: Seq[QTree[A]])(implicit sg: QTreeSemigroup[A]):
      QTree[A] = qTrees.reduce(sg.plus(_, _))
}
