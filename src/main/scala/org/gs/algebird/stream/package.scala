/**
  */
package org.gs.algebird

import com.twitter.algebird._
/** @author garystruthers
  *
  */
package object stream {

  /** Turn a sequence of value, time tuples into a seq of DecayedValues
    *
    * Wraps toDecayedValues so it can be curried to just the xs parameter for Flow.map
    * 
    * @param halfLife used to scale value based on time
    * @param last is initial element, if None use implicit monoid.zero
    * @param xs sequence of value, time tuples
    * @param monoid implicit DecayedValueMonoid used to scan from initial value
    * @return seq of DecayedValues
    */
  def curryToDecayedValues(halfLife: Double, last: Option[DecayedValue] = None)
          (xs: Seq[(Double, Double)])(implicit monoid: DecayedValueMonoid): Seq[DecayedValue] = {
    toDecayedValues(xs, halfLife, last)
  }
}