/** Estimate frequency of values
  *
  * @see https://github.com/twitter/algebird/...CountMinSketch.scala
  */
package org.gs.akka.algebird

import com.twitter.algebird._

/** @author garystruthers
  *
  */
object Countminsketch {
  val depth = 5
  val width = 5
  val seed = 0
  val heavyHittersPct = 0.05
  val a = CMS.aggregator(depth, width, seed, heavyHittersPct)
  val m = CMS.monoid(depth, width, seed, heavyHittersPct)
  val cms = m.plus(m.create(5L), m.create(6L))
  val est = cms.frequency(5L) //Approximate(0,1,1,0.9932620530009145)
  val estEst = cms.frequency(5L).estimate // Long 1
  val hh = cms.heavyHitters // TreeSet(5,6)
  a.present(cms)

  val DELTA = 1E-10
  val EPS = 0.001
  val SEED = 1
  val CMS_MONOID = new CountMinSketchMonoid(EPS, DELTA, SEED)
  val data = List(1L, 1L, 3L, 4L, 5L)
  val cms2 = CMS_MONOID.create(data)
  cms2.totalCount
  cms2.frequency(1L).estimate
  cms2.frequency(2L).estimate
  cms2.frequency(3L).estimate
  def zipFrequency(a: Long): (Long, Long) = (a, cms2.frequency(a).estimate)
  val zipped = data map zipFrequency
  val zippedDistinct = zipped.distinct

}