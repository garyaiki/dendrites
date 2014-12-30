/**
  */
package org.gs

import com.twitter.algebird._

/** @author garystruthers
  *
  */
package object algebird {

  /** Sums list elements
    * @tparam A: Semigroup element type that can be added, uses implicit Semigroup[A]
    * @param xs list
    * @return Option of sum or None for empty list
    */
  def sumOption[A: Semigroup](xs: Seq[A]): Option[A] = implicitly[Semigroup[A]].sumOption(xs)

  /** Sums list elements
    * @tparam A: Monoid element type that can be added, has zero, uses implicit Monoid[A]
    * @param xs list
    * @param ev Monoid for type A
    * @return sum or Monoid[A] zero for empty list
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

  /** Multiplies list elements
    * @tparam A: Ring element type that can be multiplied, has a one, uses implicit Ring[A]
    * @param xs list
    * @return sum or Monoid[A] zero for empty list
    */
  def product[A: Ring](xs: Seq[A]): A = implicitly[Ring[A]].product(xs)

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

  /** Field[BigDecimal] implicit
    * BigDecimal implicits supported in Algebird with NumericRing[BigDecimal]
    *
    */
  implicit object BigDecimalField extends NumericRing[BigDecimal] with Field[BigDecimal] {
    override def inverse(v: BigDecimal): BigDecimal = 1 / v
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

  /** Quickly estimate count of distinct integer values using HyperLogLog
    * 
    * @param xs sequence of integers
    * @param agg HyperLogLogAggregator, initialized with # of bits for hashing
    * @return estimate count in an Approximate object
    */
  def estDistinctVals(xs: Seq[Int])(implicit agg: HyperLogLogAggregator): Approximate[Long] = {
    agg(xs.map(HyperLogLog.int2Bytes(_))).approximateSize
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
    * @param eps
    * @param delta
    * @param seed
    * @return CountMinSketchMonoid
    */
  def createCMS_Monoid(eps: Double = 0.001, delta: Double = 1E-10, seed: Int = 1): CountMinSketchMonoid = new CountMinSketchMonoid(eps, delta, seed)

  /** Create a CMS
    * @param xs data
    * @param monoid
    * @return CMS for data
    */
  def createCountMinSketch(xs: Seq[Long])(implicit monoid: CountMinSketchMonoid): CMS = {
    monoid.create(xs)
  }

  /** Add data to existing CMS
    * @param xs data
    * @param cmsL existing CMS
    * @param monoid
    * @return cmsL ++ cmsR
    */
  def appendCountMinSketch(xs: Seq[Long])(implicit cmsL: CMS, monoid: CountMinSketchMonoid) = {
    val cmsR = monoid.create(xs)
    monoid.plus(cmsL, cmsR)
  }

  /** Turn a sequence of value, time tuples into a seq of DecayedValues
    *
    * @param latest used as initial element, if None use implicit monoid.zero else use last
    * DecayedValue
    * @param xs sequence of value, time tuples
    * @param halfLife used to scale value based on time
    * @param monoid used to scan from initial value
    * @return seq of DecayedValues
    */
  def toDecayedValues(latest: Option[DecayedValue] = None,
    xs: Seq[(Double, Double)],
    halfLife: Double)(
      implicit monoid: DecayedValueMonoid): Seq[DecayedValue] = {
    val z = latest match {
      case None => monoid.zero
      case Some(x) => x
    }
    xs.scanLeft(z) { (previous, xs) =>
      val (value, time) = xs
      monoid.plus(previous, DecayedValue.build(value, time, halfLife))
    }
  }
}