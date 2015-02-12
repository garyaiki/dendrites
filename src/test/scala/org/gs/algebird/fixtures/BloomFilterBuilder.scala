/**
  */
package org.gs.algebird.fixtures

import org.gs._
import org.gs.fixtures.SysProcessUtils
import org.gs.algebird._
import language.postfixOps
import util.Random
import org.scalatest._
import com.twitter.algebird._

/** @author garystruthers
  *
  */
trait BloomFilterBuilder extends SuiteMixin with SysProcessUtils { this: Suite =>

  abstract override def withFixture(test: NoArgTest) = {
    super.withFixture(test)
  }
 
  def testWords(wordCount: Int, wds: Seq[String], numRandoms: Int): IndexedSeq[String] = {
    val words = wds.toIndexedSeq
    val rnd = new Random
    val fromRange = 0 until wordCount
    val toRange = 0 until numRandoms
    for (i <- toRange) yield words(fromRange(rnd.nextInt(fromRange length)))
  }
}
