/** AveragedValue has a count and an average value. Summing them adds the counts and calculates
  * the total average 
  * @see https://github.com/twitter/algebird...AveragedValue.scala
  */
package org.gs.akka.algebird

import com.twitter.algebird._

/** @author garystruthers
  *
  */
object AvgValue {

  val av = AveragedValue(50, 66.6)
  val av1 = AveragedValue(13, 17.0)
  val sum = AveragedGroup.plus(av, av1)
  val isNonZero = AveragedGroup.isNonZero(sum)
  val negate = AveragedGroup negate sum
  val avs = List(AveragedValue(50, 66.6), AveragedValue(500, 56.4), AveragedValue(450, 60.9),
      AveragedValue(650, 66.1), AveragedValue(500, 56.7), AveragedValue(850, 62.8))
  val avSum = avs.reduce[AveragedValue](AveragedGroup.plus)
}