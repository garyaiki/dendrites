/** 
  *  @see https://github.com/twitter/algebird...Approximate.scala
  */
package org.gs.akka.algebird

import com.twitter.algebird._

/** @author garystruthers
  *
  */
object Approx {

  def times(x: ApproximateBoolean, y: ApproximateBoolean) = x ^ y
  def or(x: ApproximateBoolean, y: ApproximateBoolean) = x || y
  def and(x: ApproximateBoolean, y: ApproximateBoolean) = x && y

  def plus[A](x: Approximate[A], y: Approximate[A])(implicit num: Numeric[A]) = x + y
  def minus[A](x: Approximate[A], y: Approximate[A])(implicit num: Numeric[A]) = x - y
  def times[A](x: Approximate[A], y: Approximate[A])(implicit num: Numeric[A]) = x * y

  val ab = List(ApproximateBoolean(false, 0.9), ApproximateBoolean(true, 0.9),
    ApproximateBoolean(false, 0.89), ApproximateBoolean(true, 0.79),
    ApproximateBoolean(true, 0.6), ApproximateBoolean(true, 0.7))
  val probProduct = ab.reduce[ApproximateBoolean](times)
  val probOr = ab.reduce[ApproximateBoolean](or)
  val probAnd = ab.reduce[ApproximateBoolean](and)
  
  val a = List(Approximate[Int](510, 710, 900, 0.9), Approximate[Int](500, 720, 900, 0.9),
    Approximate[Int](500, 730, 900, 0.89), Approximate[Int](500, 740, 900, 0.79),
    Approximate[Int](550, 700, 900, 0.6), Approximate[Int](500, 760, 900, 0.7))
    
  val probSum = a.reduce[Approximate[Int]](plus[Int])
  val probDiff = a.reduce[Approximate[Int]](minus[Int])
  val probProd = a.reduce[Approximate[Int]](times[Int])


}