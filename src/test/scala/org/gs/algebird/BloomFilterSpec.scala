/**
  */
package org.gs.algebird

import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import org.gs.algebird.fixtures.BloomFilterBuilder
import org.gs.algebird._
import org.gs._
import com.twitter.algebird._

/** @author garystruthers
  *
  */
class BloomFilterSpec extends FlatSpecLike with BloomFilterBuilder {
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
   
  "A properNames BloomFilter" should "have 0 false negatives" in {
   for(i <- properTestWords) { assert(properBF.contains(i).isTrue)}
  }  
  
  "A connectives BloomFilter" should "have 0 false negatives" in {
   for(i <- connectivesTestWords) { assert(connectivesBF.contains(i).isTrue)}
  }
  
  "A words BloomFilter" should "have 0 false negatives" in {
   for(i <- wordsTestWords) { assert(wordsBF.contains(i).isTrue)}
  }
  
  "A properNames BloomFilter" should "have <= fpProb * 2 false positives" in {
   val falsePositives = for {
     i <- properFalseWords
     if properBF.contains(i).isTrue
   } yield i
   assert(falsePositives.size <= properNames.size * (fpProb * 2))
  }

  "A connectives BloomFilter" should "have <= fpProb * 2 false positives" in {
   val falsePositives = for {
     i <- connectivesFalseWords
     if connectivesBF.contains(i).isTrue
   } yield i
   assert(falsePositives.size < connectives.size * (fpProb * 2))
  }

  "A words BloomFilter" should "have < fpProb false positives" in {
   val falsePositives = for {
     i <- wordsFalseWords
     if wordsBF.contains(i).isTrue
   } yield i
   assert(falsePositives.size < words.size * fpProb)
  }
}