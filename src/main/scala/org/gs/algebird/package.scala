/**
  */
package org.gs

import com.twitter.algebird._
import org.gs.algebird.typeclasses.HyperLogLogLike
import org.gs.algebird.typeclasses.QTreeLike
/** Aggregation functions for distributed systems. Simplifies using Twitter Algebird.
  *
  * Algebird provides implicit implementations of common types which are imported here. Class
  * extraction methods in org.gs can be used to extract a field from your case classes and tuples.
  * When the extracted field is an already supported type, you don't have to write custom Algebird
  * classes.
  * @see org.gs.package
  *
  * ScalaTests are in src/test/scala/org.gs.algebird
  *
  * ==AveragedValue find local average then sum them to global average==
  * @see org.gs.algebird.AveragedSpec.scala
  *
  * Average a Sequence of values
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
  * ==BloomFilter fast find if a word is in a dictionary==
  * OSX has dictionaries you can use to create BloomFilters
  * @see org.gs.fixtures.SysProcessUtils.scala for Paths to properNames, connectives, and words and
  * functions to read their words
  * @see org.gs.algebird.fixtures.BloomFilterBuilder.scala for creation of BloomFilters for these
  * dictionaries and select test words for them.
  * @see org.gs.algebird.BloomFilterSpec.scala
  *
  * Create a BloomFilter for OSX words dictionary
  * {{{
  * val falsePositivepProb: Double = 0.01
  * val words = readWords(wordsPath)
  * val wordsBF = createBF(words, fpProb)
  * }}}
  *
  * Is word in BloomFilter
  * {{{
  * val falsePositivepProb: Double = 0.01
  * val word = "path"
  * val inDict = wordsBF.contains(word).isTrue
  * }}}
  *
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
  *
  * ==CountMinSketch==
  * @see org.gs.algebird.CountMinSketchSpec.scala
  * Test data is IP addresses repeated a random number of times
  * @see org.gs.fixtures.InetAddressesBuilder.scala
  *
  * Estimate total number of elements seen so far
  * {{{
  * val addrs = inetAddresses(ipRange)
  * val longZips = inetToLongZip(addrs)
  * val longs = testLongs(longZips)
  *
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
  * ==DecayedValue==
  * @see org.gs.algebird.DecayedValueSpec.scala
  * Test data is a sine wave with a value for each of 360 degrees with a corresponding time value
  * The idea is a rising and falling value over a year
  * @see org.gs.fixtures.TrigUtils
  *
  * Moving average from the initial value to specified index
  * {{{
  * val sines = genSineWave(100, 0 to 360)
  * val days = Range.Double(0.0, 361.0, 1.0)
  * val sinesZip = sines.zip(days)
  *
  * val decayedValues = toDecayedValues(sinesZip, 10.0, None)
  * val avgAt90 = decayedValues(90).average(10.0)
  * }}}
  *
  * Moving average from specified index to specified index
  * {{{
  * val avg80to90 = decayedValues(90).averageFrom(10.0, 80.0, 90.0)
  * }}}
  *
  * ==HyperLogLog==
  * @see org.gs.algebird.HyperLogLogSpec.scala
  *
  * Create a HLL from a sequence of Int
  * {{{
  * implicit val ag = HyperLogLogAggregator(12)
  * val ints: Seq[Int]
  * val hll = createHLL(ints)
  * }}}
  * Create a sequence of HLL
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
  * val hlls = Vector(hll, hll2)
  * val approxs = mapHLL2Approximate(hlls)
  * }}}
  * Sum a Sequence of Approximate and estimate total size
  * {{{
  * val sum = approxs.reduce(_ + _)
  * }}}
  *
  * ==QTree==
  * @see org.gs.algebird.QTreeSpec.scala
  * Build QTree from a Sequence
  * @see org.gs.algebird.fixtures.QTreeBuilder
  * {{{
  * val level = 5
  * implicit val qtBDSemigroup = new QTreeSemigroup[BigDecimal](level)
  * val qtBD = buildQTree[BigDecimal](bigDecimals)
  * }}}
  * Get its InterQuartileMean
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
  * ==Functor, map, andThen for Sequence types==
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
  * ==Max Min for Sequence types that have a Semigroup, Monoid and Ordering==
  * @see org.gs.algebird.MaxSpec.scala
  * @see org.gs.algebird.MinSpec.scala
  *
  * Get Max element of a sequence.
  * {{{
  * val iqm = qtBD.interQuartileMean
  * 
  * val bigDecimals: Seq[BigDecimal]
  * val max = max(bigDecimals)
  * val optBigDecs: [Option[BigDecimal]]
  * val max2 = max(optBigDecs.flatten)
  * Either can be used to return errors from remote system
  * val eithBigInts = Seq[Either[String, BigInt]]
  * val max3 = max(filterRight(eithBigInts)
  * }}}
  *
  * Get Min element of a sequence.
  * {{{
  * val bigDecimals: Seq[BigDecimal]
  * val min = min(bigDecimals)
  * val optBigDecs: [Option[BigDecimal]]
  * val min2 = min(optBigDecs.flatten)
  * Either can be used to return errors from remote system
  * val eithBigInts = Seq[Either[String, BigInt]]
  * val min3 = min(filterRight(eithBigInts)
  * }}}
  *
  * ==Semigroup, plus function obeys associtive law in asychronous system==
  * @see org.gs.algebird.SemigroupSpec.scala
  *
  * Sum elements of a sequence that may be empty.
  * {{{
  * val bigDecimals: Seq[BigDecimal]
  * val opt = sumOption(bigDecimals)
  * val sum = opt.get
  * Either can be used to return errors from remote system
  * val eithBigInts = Seq[Either[String, BigInt]]
  * val eith = sumOption(eithBigInts).get
  * val sum2 = eith.right.get
  * }}}
  *
  * ==Monoid extends Semigroup with zero element==
  * @see org.gs.algebird.MonoidSpec.scala
  *
  * Sum elements of a sequence for a type that has a zero under addition.
  * {{{
  * val doubles: Seq[Double]
  * val sum = sum(doubles)
  * val optBigInts: Seq[Option[BigInt]]
  * val sum2 = sum(optBigInts.flatten)
  * Either can be used to return errors from remote system
  * val eithFloats = Seq[Either[String, Float]]
  * val sum3 = sum(eithFloats).right.get
  * }}}
  *
  * ==Group extends Monoid with minus, negate==
  * @see org.gs.algebird.GroupSpec.scala
  *
  * Negate a value
  * {{{
  * val float = 3131.7f
  * val neg = negate(float)
  * val double = 3130.0
  * val neg2 = negate(double.get)
  * Either can be used to return errors from remote system
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
  * Either can be used to return errors from remote system
  * val eithLong = 2847L
  * val diff3 = minus(eithLong.right.get, 47L)
  * }}}
  *
  * ==Ring extends Group with times, product and one identity under multiplication==
  * @see org.gs.algebird.RingSpec.scala
  *
  * Multiply a value by another
  * {{{
  * val float = 3131.7f
  * val prod = times(float, 1.7f)
  * val opt = Some(3130.0)
  * val prod2 = times(opt.get, 30.0)
  * Either can be used to return errors from remote system
  * val eithLong = 2847L
  * val prod3 = times(eithLong.right.get, 47L)
  * }}}
  *
  * Multiply elements of a sequence (((xs[0] * xs[1]) * xs[2]) * xs[3]) for a type that has a one.
  * {{{
  * val doubles: Seq[Double]
  * val prod = product(doubles)
  * val optBigInts: Seq[Option[BigInt]]
  * val prod2 = product(optBigInts.flatten)
  * Either can be used to return errors from remote system
  * val eithFloats = Seq[Either[String, Float]]
  * val prod3 = product(eithFloats).right.get
  * }}}
  *
  * ==Field extends Ring with inverse, div==
  * @see org.gs.algebird.FieldSpec.scala
  *
  * Invert a value a -> 1/a
  * {{{
  * val float = 3131.7f
  * val recip = inverse(float)
  * }}}
  * {{{
  * val optDouble = Some(3130.0)
  * val recip = inverse(double.get)
  * }}}
  * Either can be used to return errors from remote system
  * val eithInt = Right(2847)
  * val recip = inverse(eithInt.right.get)
  * }}}
  *
  * Dividee a value by another
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
  * @author garystruthers
  */
package object algebird {

  /** Sums sequence elements
    * @tparam A: Semigroup element type that can be added, uses implicit Semigroup[A]
    * @param xs sequence
    * @return Some(sum) or None if xs empty
    */
  def sumOption[A: Semigroup](xs: Seq[A]): Option[A] = implicitly[Semigroup[A]].sumOption(xs)

  /** Sums sequence elements
    * @tparam A: Monoid element type that can be added, has zero, uses implicit Monoid[A]
    * @param xs sequence
    * @return sum or Monoid[A] zero for empty sequence
    */
  def sum[A: Monoid](xs: Seq[A]): A = implicitly[Monoid[A]].sum(xs)

  /** Negates an element
    * @tparam A: Group element type that can be negated, uses implicit Group[A]
    * @param x element to negate
    * @return -x
    */
  def negate[A: Group](x: A): A = implicitly[Group[A]].negate(x)

  /** Subtracts an element from another
    * @tparam A: Group element type that can be negated, uses implicit Group[A]
    * @param l
    * @param r
    * @return l - r
    */
  def minus[A: Group](l: A, r: A): A = implicitly[Group[A]].minus(l, r)

  /** Multiplies an element by another
    * @tparam A: Ring element type that can be multiplied, uses implicit Ring[A]
    * @param l
    * @param r
    * @return l * r
    */
  def times[A: Ring](l: A, r: A): A = implicitly[Ring[A]].times(l, r)

  /** Multiplies sequence elements
    * @tparam A: Ring element type that can be multiplied, has a one, uses implicit Ring[A]
    * @param xs sequence
    * @return sum or Monoid[A] zero for empty sequence
    */
  def product[A: Ring](xs: Seq[A]): A = implicitly[Ring[A]].product(xs)

  /** Reciprocal of an element
    * @tparam A: Field element type that can divide 1 and there is an implicit Field[A]
    * @param x element to divide one
    * @return 1/x
    */
  def inverse[A: Field](x: A): A = implicitly[Field[A]].inverse(x)

  /** Divide an element by another
    * @tparam A: Field element type that can be divided, uses implicit Field[A]
    * @param l
    * @param r
    * @return l * r
    */
  def div[A: Field](l: A, r: A): A = implicitly[Field[A]].div(l, r)

  /** Find maximum element in a Seq
    * @tparam A: Ordering element type that can be ordered, uses Ordering[A]
    * @param xs Seq[A]
    * @return max element
    */
  def max[A: Ordering](xs: Seq[A]): A = MaxAggregator[A].reduce(xs)

  /** Find minum element in a Seq
    * @tparam A: Ordering element type that can be ordered, uses Ordering[A]
    * @param xs Seq[A]
    * @return min element
    */
  def min[A: Ordering](xs: Seq[A]): A = MinAggregator[A].reduce(xs)

  /** Field[BigDecimal] implicit
    * BigDecimal implicits supported in Algebird with NumericRing[BigDecimal]
    *
    */
  implicit object BigDecimalField extends NumericRing[BigDecimal] with Field[BigDecimal] {
    override def inverse(v: BigDecimal): BigDecimal = {
      assertNotZero(v)
      1 / v
    }
  }

  /** Field[BigInt] implicit
    * BigInt implicits supported in Algebird with NumericRing[BigInt]
    *
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
    */
  implicit object IntField extends Field[Int] {
    def zero = IntRing.zero
    def one = IntRing.one
    override def negate(v: Int) = IntRing.negate(v)
    def plus(l: Int, r: Int) = IntRing.plus(l, r)
    override def minus(l: Int, r: Int) = IntRing.minus(l, r)
    def times(l: Int, r: Int) = IntRing.times(l, r)
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
    */
  implicit object LongField extends Field[Long] {
    def zero = LongRing.zero
    def one = LongRing.one
    override def negate(v: Long) = LongRing.negate(v)
    def plus(l: Long, r: Long) = LongRing.plus(l, r)
    override def minus(l: Long, r: Long) = LongRing.minus(l, r)
    def times(l: Long, r: Long) = LongRing.times(l, r)
    override def div(l: Long, r: Long): Long = {
      assertNotZero(r)
      l / r
    }
    override def inverse(v: Long): Long = {
      assertNotZero(v)
      1 / v
    }
  }
  /** map sequence[A] to sequence[B] */
  implicit object SeqFunctor extends Functor[Seq] {
    def map[A, B](fa: Seq[A])(f: A => B): Seq[B] = (for (a <- fa) yield f(a))
  }

  /** Compose 2 functors map Seq[A] -> Seq[B] -> Seq[C]
    * @param fa Seq[A]
    * @param f A => B
    * @param g B => C
    * @param ev implicit Functor[Seq]
    * @return Seq[C]
    */
  def andThen[A, B, C](fa: Seq[A])(f: A => B)(g: B => C)(implicit ev: Functor[Seq]): Seq[C] = {
    ev.map(ev.map(fa)(f))(g)
  }

  /** AverageValue of a Seq of Numeric elements
    *
    * @tparam A: Numeric
    * @param xs Seq
    * @param evidence implicit Numeric[A]
    * @return AverageValue
    */
  def avg[A: Numeric](xs: Seq[A]): AveragedValue = {
    val at = andThen[A, Double, AveragedValue](xs)(implicitly[Numeric[A]].toDouble)(
      Averager.prepare(_))
    at.reduce(AveragedGroup.plus(_, _))
  }

  def sumAverageValues(xs: Seq[AveragedValue]): AveragedValue = {
    xs.reduce(AveragedGroup.plus(_, _))
  }
  /** Create BloomFilter configure and load it from a Seq of words
    *
    * @param words
    * @param fpProb false positive probability, 1% default
    * @return BloomFilter
    */
  def createBF(words: Seq[String], fpProb: Double = 0.01): BF = {
    val wc = words.size
    val bfMonoid = BloomFilter(wc, fpProb)
    bfMonoid.create(words: _*)
  }

  /** Quickly find strings in and not in Bloom filter
    * @param xs strings to test
    * @param bf configured and data initialized Bloom filter
    * @return tuple ._1 is matches including false positives, ._2 are not in BF
    */
  def bloomFilterPartition(xs: Seq[String])(implicit bf: BF): (Seq[String], Seq[String]) =
    xs.partition(bf.contains(_).isTrue)

  /** Convience creator with default parameters
    * @tparam K type of elements to be counted
    * @param eps
    * @param delta
    * @param seed
    * @return CMSMonoid[K]
    */
  def createCMSMonoid[K: Ordering: CMSHasher](eps: Double = 0.001,
    delta: Double = 1E-10,
    seed: Int = 1): CMSMonoid[K] = new CMSMonoid[K](eps, delta, seed)

  /** Create a CMS
    * @tparam K type of elements to be counted
    * @param xs data
    * @param monoid
    * @return CMS for data
    */
  def createCountMinSketch[K: Ordering: CMSHasher](xs: Seq[K])(implicit monoid: CMSMonoid[K]): CMS[K] = {
    monoid.create(xs)
  }

  /** Sum a Sequence of CMS
    * @tparam K type of elements in CMS
    * @param xs Sequence of CMS
    * @param monoid
    * @return CMS as the sum of Sequence of CMS
    */
  def sumCountMinSketch[K: Ordering: CMSHasher](xs: Seq[CMS[K]])(implicit monoid: CMSMonoid[K]): CMS[K] = {
    xs.reduce(monoid.plus(_, _))
  }

  /** Turn a sequence of value, time tuples into a seq of DecayedValues
    *
    * @param xs sequence of value, time tuples
    * @param halfLife used to scale value based on time
    * @param last is initial element, if None use implicit monoid.zero
    * @param monoid used to scan from initial value
    * @return seq of DecayedValues
    */
  def toDecayedValues(xs: Seq[(Double, Double)],
    halfLife: Double,
    last: Option[DecayedValue] = None)(implicit monoid: DecayedValueMonoid): Seq[DecayedValue] = {
    val z = last match {
      case None => monoid.zero
      case Some(x) => x
    }

    def op(previous: DecayedValue, x: (Double, Double)): DecayedValue = {
      val (value, time) = x
      val d = time match {
        case x if (time < 1.0) => 1.0
        case x if (time < halfLife) => time
        case _ => halfLife
      }
      monoid.plus(previous, DecayedValue.build(value, time, d))
    }
    xs.scanLeft(z)(op)
  }

  /** Create HyperLogLog
    *
    * @tparam A: Int, Long
    * @param xs sequence of integers or longs
    * @param agg HyperLogLogAggregator, initialized with # of bits for hashing
    * @return an HLL
    */
  def createHLL[A: HyperLogLogLike](xs: Seq[A])(
    implicit ev: HyperLogLogLike[A], agg: HyperLogLogAggregator): HLL = ev(xs)

  /** Map Sequence of HyperLogLogs to Sequence of Approximate
    *
    * @param xs sequence of HLL
    * @return Sequence of Approximate
    */
  def mapHLL2Approximate(xs: Seq[HLL]): Seq[Approximate[Long]] = {
    xs.map(_.approximateSize)
  }

  /** Estimate total count of distinct integer values in multiple HyperLogLogs
    *
    * @param xs sequence of HLL
    * @return estimate count in an Approximate object
    */
  def sumHLLApproximateSizes(xs: Seq[HLL]): Approximate[Long] = {
    xs.reduce(_ + _).approximateSize
  }

  /** Build a QTree from a Seq
    *
    * @tparam A: BigDecimal, BigInt, Double, Float, Int, Long
    * @param vals
    * @param ev Typeclass to build from Seq
    * @param sg
    * @return
    */
  def buildQTree[A: QTreeLike](vals: Seq[A])(
        implicit ev: QTreeLike[A], sg: QTreeSemigroup[A]): QTree[A] = {
    vals.map { ev(_) }.reduce { sg.plus(_, _) }
  }

  /** Sum a Sequence of QTrees
    *
    * @tparam A: BigDecimal, BigInt, Double, Float, Int, Long
    * @param vals
    * @param ev Typeclass to build from Seq
    * @param sg
    * @return
    */
  def sumQTrees[A: QTreeSemigroup](qTrees: Seq[QTree[A]])(implicit sg: QTreeSemigroup[A]): QTree[A] =
    qTrees.reduce(sg.plus(_, _))
}
