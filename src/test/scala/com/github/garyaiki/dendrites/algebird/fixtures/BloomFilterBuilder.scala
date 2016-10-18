/** Copyright 2016 Gary Struthers

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.github.garyaiki.dendrites.algebird.fixtures

import org.scalatest.{TestSuite, TestSuiteMixin}
import scala.language.postfixOps
import util.Random
import com.github.garyaiki.dendrites.fixtures.SysProcessUtils
import com.github.garyaiki.dendrites.algebird.createBF

/**
  *
  * @author Gary Struthers
  */
trait BloomFilterBuilder extends TestSuiteMixin with SysProcessUtils { this: TestSuite =>

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
