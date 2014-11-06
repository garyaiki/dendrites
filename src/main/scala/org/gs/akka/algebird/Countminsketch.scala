/**
  */
package org.gs.akka.algebird

import com.twitter.algebird._

/** @author garystruthers
  *
  */
object Countminsketch {
  val DELTA = 1E-10
  val EPS = 0.001
  val SEED = 1
  val CMS_MONOID = new CountMinSketchMonoid(EPS, DELTA, SEED)
  val data = List(1L, 1L, 3L, 4L, 5L)
  val cms = CMS_MONOID.create(data)
  cms.totalCount
  cms.frequency(1L).estimate
  cms.frequency(2L).estimate
  cms.frequency(3L).estimate
  def zipFrequency(a: Long):(Long, Long) = (a, cms.frequency(a).estimate)
  val zipped = data map zipFrequency
  val zippedDistinct = zipped.distinct

}