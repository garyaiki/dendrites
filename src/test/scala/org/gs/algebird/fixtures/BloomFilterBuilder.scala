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
/*
  def fromFile(path: String): (Int, Seq[String]) = {
    val wc = wordCount(path)
    val words = readWords(path)
    (wc, words)
  }
  
  def createBF(numAndEntries: (Int, Seq[String]), fpProb: Double = 0.01): BF = {
    val wc = numAndEntries._1
    val words = numAndEntries._2
    val bfMonoid = BloomFilter(wc, fpProb)
    bfMonoid.create(words: _*)
  }
*/  
  def testWords(wordCount: Int, wds: Seq[String], numRandoms: Int): IndexedSeq[String] = {
    val words = wds.toIndexedSeq
    val rnd = new Random
    val fromRange = 0 until wordCount
    val toRange = 0 until numRandoms
    for (i <- toRange) yield words(fromRange(rnd.nextInt(fromRange length)))
  }
}