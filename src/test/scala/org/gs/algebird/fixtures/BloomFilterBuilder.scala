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

  val fpProb: Double = 0.01

  val properNames = readWords(properNamesPath)
  val properTestWords = testWords(properNames.size, properNames, 100)
  val properFalseWords = for (i <- properTestWords) yield i.toUpperCase()
  val properBF = createBF(properNames, fpProb)

  val connectives = readWords(connectivesPath)
  val connectivesTestWords = testWords(connectives.size, connectives, 100)
  val connectivesFalseWords = for (i <- connectivesTestWords) yield i.toUpperCase()
  val connectivesBF = createBF(connectives, fpProb)

  val words = readWords(wordsPath)
  val wordsTestWords = testWords(words.size, words, 10000)
  val wordsFalseWords = for (i <- wordsTestWords) yield i.toUpperCase()
  val wordsBF = createBF(words, fpProb)
}
