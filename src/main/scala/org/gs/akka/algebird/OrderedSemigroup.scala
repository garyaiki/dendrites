/** aggregate comparable objects, numerics, and strings
  *
  * @see https://github.com/twitter/algebird...OrderedSemigroup.scala
  */
package org.gs.akka.algebird

import math.Ordered

import com.twitter.algebird._
import com.twitter.algebird.Operators._

/** @author garystruthers
  *
  */
object OrderedSemigroup {

  def maxResult[T <: Ordered[T]](x: T, y: T) = (Max(x) + Max(y)).get
  def maxResult(x: String, y: String) = (Max(x) + Max(y)).get
  def maxNumericResult[T](x: T, y: T)(implicit num: Numeric[T]) = (Max(x) + Max(y)).get

  def minResult[T <: Ordered[T]](x: T, y: T) = (Min(x) + Min(y)).get
  def minResult(x: String, y: String) = (Min(x) + Min(y)).get
  def minNumericResult[T](x: T, y: T)(implicit num: Numeric[T]) = (Min(x) + Min(y)).get

  def firstResult[T <: Ordered[T]](x: T, y: T) = (First(x) + First(y)).get
  def firstResult(x: String, y: String) = (First(x) + First(y)).get
  def firstNumericResult[T](x: T, y: T)(implicit num: Numeric[T]) = (First(x) + First(y)).get

  def lastResult[T <: Ordered[T]](x: T, y: T) = (Last(x) + Last(y)).get
  def lastResult(x: String, y: String) = (Last(x) + Last(y)).get
  def lastNumericResult[T](x: T, y: T)(implicit num: Numeric[T]) = (Last(x) + Last(y)).get

}