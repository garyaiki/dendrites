/**
  */
package org.gs.fixtures

import com.github.nscala_time.time.Imports._
import org.joda.time.base.BaseSingleFieldPeriod
import org.joda.time.{ Seconds, Minutes, Hours, Days, Weeks, Months, Years }
import scala.language.postfixOps

/** @author garystruthers
  *
  */
trait NScalaTimeUtils {
  implicit val initialDT = (new DateTime).withYear(2015).withMonthOfYear(1).withDayOfMonth(1)

  def incDateTime(inc: Int, period: BaseSingleFieldPeriod)(implicit dt: DateTime): DateTime = {
    period match {
      case x: Seconds => dt + inc.seconds
      case x: Minutes => dt + inc.minutes
      case x: Hours => dt + inc.hours
      case x: Days => dt + inc.days
      case x: Weeks => dt + inc.weeks
      case x: Months => dt + inc.months
      case x: Years => dt + inc.years
    }
  }

  def createPeriodRange(r: Range, period: BaseSingleFieldPeriod): Seq[DateTime] = {
    for {
      i <- r
    } yield incDateTime(i, period)
  }
  
  def toLongs(xs: Seq[DateTime]): Seq[Long] = xs.map( _.getMillis)

  def toDoubles(xs: Seq[DateTime]): Seq[Double] = xs.map( _.getMillis.toDouble)
}
object app extends NScalaTimeUtils {
  def main(args: Array[String]): Unit = {
    val days = createPeriodRange(0 until 5, Days.ZERO)
    val longs = toLongs(days)
    import math.sin
    import math.Pi
    val dr = 0 to 40
    val twoPi = Pi * 2
    for {
      t <- dr
    } print(s"t:$t ${sin(t.toDouble / twoPi)} ")
      
//    } print(s"t:$t ${100 * sin((t % 6).toDouble)} ")
/*
    for (i <- createPeriodRange(0 until 5, Seconds.ZERO)) print(s"seconds:$i ");println
    for (i <- createPeriodRange(0 until 5, Minutes.ZERO)) print(s"minutes:$i ");println
    for (i <- createPeriodRange(0 until 5, Hours.ZERO)) print(s"hours:$i ");println
    for (i <- createPeriodRange(0 until 5, Days.ZERO)) print(s"days:$i ");println
    for (i <- createPeriodRange(0 until 6, Weeks.ZERO)) print(s"weeks:$i ");println
    for (i <- createPeriodRange(0 until 5, Months.ZERO)) print(s"months:$i ");println
    for (i <- createPeriodRange(0 until 5, Years.ZERO)) print(s"years:$i ");println
    */
  }
}