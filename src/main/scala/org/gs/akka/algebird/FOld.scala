/** @see https://github.com/twitter/algebird/...Fold.scala
  */
package org.gs.akka.algebird

import com.twitter.algebird._
import scala.collection.mutable.ArrayBuffer

/** @author garystruthers
  *
  */
object FOld {

  val fl = Fold.foldLeft[String, String]("") { (a, b) => a ++ b }
  val chars = List("a", "b", "c", "d", "e")
  val sfl = fl.overTraversable(chars)
  case class DesctiptiveFold(desc: String, result: String)
  val desc = "a description"
  val f = Fold.fold[String, String, DesctiptiveFold]((a, b) => a ++ b, "", a => DesctiptiveFold(desc, a))
  val s = f.overTraversable(chars)
  def fu = (a: Unit) => ""
  val fm = Fold.foldMutable[String, String, ArrayBuffer[String]]((a, b) => a ++ b, fu, a => ArrayBuffer(a))
  val sm = fm.overTraversable(chars)
  val fs = Fold.sequence(Seq(fl, f, fm))
  val ss = fs.overTraversable(chars)
  val fjw = Fold.sum[Int].joinWith(Fold.size) { (s, c) => s.toDouble / c }
  val ints = List(1,2,3,4,5,6,7,8,9)
  val avg = fjw.overTraversable(ints)
}