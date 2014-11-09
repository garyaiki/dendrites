/**
  */
package org.gs.akka.algebird

import com.twitter.algebird._

/** @author garystruthers
  *
  */
object Field {

  val ff = FloatField
  val one = ff.one
  val zero = ff.zero
  val negate = ff.negate(2.0f)
  val plus = ff plus (1.0f, 2.0f)
  val minus = ff minus (1.0f, 2.0f)
  val times = ff times (1.0f, 2.0f)
  val div = ff div (1.0f, 2.0f)

  val df = DoubleField
  val dfone = df.one
  val dfzero = df.zero
  val dfnegate = df.negate(2.0)
  val dfplus = df plus (1.0, 2.0)
  val dfminus = df minus (1.0, 2.0)
  val dftimes = df times (1.0, 2.0)
  val dfdiv = df div (1.0, 2.0)

  val bf = BooleanField
  val bfone = bf.one
  val bfzero = bf.zero
  val bfnegate = bf.negate(true)
  val bfplus = bf plus (true, true)
  val bfminus = bf minus (true, true)
  val bftimes = bf times (true, true)
  val bfinverse = bf inverse (true) // error on false
  val bfdiv = bf div (false, true)

}