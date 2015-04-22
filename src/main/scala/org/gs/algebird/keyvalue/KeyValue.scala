/**
  */
package org.gs.algebird.keyvalue

import com.twitter.algebird._
import org.gs.filters._

/** @author garystruthers
  *
  */
object TypedKeyValue {

  case class KeyValue[A, B](k: A, v: B)

  val kvB = List(KeyValue("a", true), KeyValue("b", false), KeyValue("c", true), KeyValue("d", false))
  val kvBD = List(KeyValue("a", BigDecimal(1)), KeyValue("b", BigDecimal(2)),
    KeyValue("c", BigDecimal(3)), KeyValue("d", BigDecimal(4)))
  val kvBI = List(KeyValue("a", BigInt(1)), KeyValue("b", BigInt(2)),
    KeyValue("c", BigInt(3)), KeyValue("d", BigInt(4)))
  val kvD = List(KeyValue("a", 1.0), KeyValue("b", 2.0), KeyValue("c", 3.0), KeyValue("d", 4.0))
  val kvF = List(KeyValue("a", 1.0f), KeyValue("b", 2.0f), KeyValue("c", 3.0f), KeyValue("d", 4.0f))
  val kvI = List(KeyValue("a", 1), KeyValue("b", 2), KeyValue("c", 3), KeyValue("d", 4))
  val kvL = List(KeyValue("a", 1L), KeyValue("b", 2L), KeyValue("c", 3L), KeyValue("d", 4L))
  val kvS = List(KeyValue("a", "1"), KeyValue("b", "2"), KeyValue("c", "3"), KeyValue("d", "4"))

  val kvEI = List(KeyValue("a", Right(1)), KeyValue("b", Left("b msg")), KeyValue("c", Right(3)), KeyValue("d", Left("d msg")))
  val kvRI = List(KeyValue("a", Right(1)), KeyValue("b", Right(2)), KeyValue("c", Right(3)), KeyValue("d", Right(4)))
  val kvEBD = List(KeyValue("a", Right(BigDecimal(1))), KeyValue("b", Left("b msg ")),
    KeyValue("c", Right(BigDecimal(3))), KeyValue("d", Left("d msg ")))
  val kvRBD = List(KeyValue("a", Right(BigDecimal(1))), KeyValue("b", Right(BigDecimal(2))),
    KeyValue("c", Right(BigDecimal(3))), KeyValue("d", Right(BigDecimal(4))))


  val ap = extractElementByIndex[BigDecimal](kvBD, 1)
  val aBIp = extractElementByIndex[BigInt](kvBI, 1)

  val bp = extractElementByIndex[Double](kvD, 1)
  val cp = extractElementByIndex[Int](kvI, 1)
  val dp = extractElementByIndex[Long](kvL, 1)
  val ep = extractElementByIndex[String](kvS, 1)
  val fp = extractElementByIndex[Boolean](kvB, 1)
  val ffp = extractElementByIndex[Float](kvF, 1)
  val cEp = extractElementByIndex[Either[String, Int]](kvEI, 1)
  val cRp = extractElementByIndex[Either[String, Int]](kvRI, 1)
  val aEp = extractElementByIndex[Either[String, BigDecimal]](kvEBD, 1)
  val aRp = extractElementByIndex[Either[String, BigDecimal]](kvRBD, 1)

  val tSBD = List(("a", BigDecimal(1)), ("b", BigDecimal(2)),
    ("c", BigDecimal(3)), ("d", BigDecimal(4)))
  val tSD = List(("a", 1.0), ("b", 2.0), ("c", 3.0), ("d", 4.0))
  val tSI = List(("a", 1), ("b", 2), ("c", 3), ("d", 4))
  val tSL = List(("a", 1L), ("b", 2L), ("c", 3L), ("d", 4L))
  val tSS = List(("a", "1"), ("b", "2"), ("c", "3"), ("d", "4"))

  val atBD = extractElementByIndex[BigDecimal](tSBD, 1)
  val btD = extractElementByIndex[Double](tSD, 1)
  val ctI = extractElementByIndex[Int](tSI, 1)
  val dtL = extractElementByIndex[Long](tSL, 1)
  val etS = extractElementByIndex[String](tSS, 1)

  import org.gs.algebird._
  val bdf = BigDecimalField
  // val bds = BigDecimalSemigroup
  sumOption(ap)(bdf)
  sumOption(atBD)(bdf)
  val es = Semigroup.eitherSemigroup(Semigroup.stringSemigroup, bdf)
  sumOption(aEp)(es)
  sumOption(aRp)(es)
  sumOption(atBD)(bdf)
  sumOption(aBIp)
  sumOption(bp)
  sumOption(cp)
  sumOption(dp)
  sumOption(ep)
  val boolsg = Semigroup.boolSemigroup
  sumOption(fp)(boolsg)
  sumOption(ffp)
  sumOption(cEp)
  sumOption(cRp)
  //  val bdm = BigDecimalMonoid
  sum(List[BigDecimal]())(bdf)
  sum(List[Boolean]())
  sum(List[Double]())
  val em = Monoid.eitherMonoid(Semigroup.stringSemigroup, bdf)
  sum(List[Either[String, BigDecimal]]())(em)
  sum(List[Either[String, Int]]())
  sum(List[Float]())
  sum(List[Int]())
  sum(List[Long]())
  sum(List[String]())
  sum(ap)(bdf)
  sum(aBIp)
  sum(atBD)(bdf)
  sum(aEp)(em)
  sum(aRp)(em)
  sum(bp)
  sum(cp)
  sum(dp)
  sum(ep)
  sum(fp)
  sum(ffp)
  sum(cEp)
  sum(cRp)
  // negate(true) could be ! but confusing
  negate(BigDecimal(1))(bdf)
  negate(BigInt(1))
  negate(1.0)
  negate(1.0f)
  negate(1)
  negate(1L)
  //negate("a") nonsense
  inverse(BigDecimal(1))(bdf)
  minus(BigDecimal(20.0), BigDecimal(10.0))
  minus(BigInt(20), BigInt(10))
  minus(20.0, 10.0)
  minus(20.0f, 10.0f)
  minus(20, 10)
  minus(20L, 10L)
  times(BigDecimal(20.0), BigDecimal(10.0))
  times(BigInt(20), BigInt(10))
  times(20.0, 10.0)
  times(20.0f, 10.0f)
  times(20, 10)
  times(20L, 10L)
  product(ap)
  product(aBIp)
  product(cp)
  product(dp)
  product(fp)
  product(ffp)

  inverse(BigDecimal(1)) //(bdf)
  inverse(7.0)
  inverse(7.0f)
  def wrapMax[Int](x: Int) = Max(x)
  val wm = SeqFunctor.map[Int, Max[Int]](List(1,2,3,4))(wrapMax)
  val mZero = Max(0)
  val mA = MaxAggregator[Int]
mA.reduce(List(1,2,3,4))
  def max[A: Ordering](xs: Seq[A]): A = MaxAggregator[A].reduce(xs)

  SeqFunctor.map[BigDecimal, BigDecimal](ap)(inverse)
  SeqFunctor.map[BigDecimal, BigDecimal](ap)(negate)

  andThen[BigDecimal, BigDecimal, BigDecimal](ap)( inverse)( negate)
  Max(5)
  import com.twitter.algebird.Operators._
  def maxNumericResult[T](x: T, y: T)(implicit num: Numeric[T]) = (Max(x) + Max(y)).get
  val mL = Max(List(1, 2, 3, 4))
  val mx = Max
  val mxl = mx.listMonoid[Int]
  def max[T](x: T, y: T) = {
    val m1 = Max(x)
    val m2 = Max(y)

  }

  val et2 = extractTuple2ByIndex[String, BigDecimal](kvBD, 0, 1)
  val et2a = (et2.toMap)
  val et2ma = et2a.+(("e", BigDecimal(5)))
  val rContains = MapAlgebra.rightContainsLeft(et2ma, et2.toMap)
  val rContains2 = MapAlgebra.rightContainsLeft(et2.toMap, et2ma)
  val et2B = extractTuple2ByIndex[String, BigDecimal](kvB, 0, 1)
  val rContains3 = MapAlgebra.rightContainsLeft(et2.toMap, et2B.toMap)
  val et2Zero = et2.:+(("e", BigDecimalField.zero))
  val removeZeros = MapAlgebra.removeZeros(et2Zero.toMap)
  val sumByKey = MapAlgebra.sumByKey(et2Zero ++ et2Zero)
  val joinByKey = MapAlgebra.join(et2Zero.toMap, et2B.toMap)
  val toGraph = MapAlgebra.toGraph(et2Zero.toMap)
  def square(x: BigDecimal) = if (x % 2 == 0) Some(x * x) else None

}