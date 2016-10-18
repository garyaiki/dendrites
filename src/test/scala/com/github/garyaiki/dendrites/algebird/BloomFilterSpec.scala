/**
  */
package com.github.garyaiki.dendrites.algebird

import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import com.github.garyaiki.dendrites.algebird.fixtures.BloomFilterBuilder
import com.github.garyaiki.dendrites.algebird._
import com.github.garyaiki.dendrites._
import com.twitter.algebird._

/** @author garystruthers
  *
  */
class BloomFilterSpec extends FlatSpecLike with BloomFilterBuilder {

  
  "A properNames BloomFilter" should "have 0 false negatives" in {
   for(i <- properTestWords) { assert(properBF.contains(i).isTrue)}
  }

  it should "have fewer false positives than the false positives probability" in {
   val fpProb: Double = 0.02
   val falsePositives = for {
     i <- properFalseWords
     if properBF.contains(i).isTrue
   } yield i
   assert(falsePositives.size <= properNames.size * fpProb)
  }
  
  "A connectives BloomFilter" should "have 0 false negatives" in {
   for(i <- connectivesTestWords) { assert(connectivesBF.contains(i).isTrue)}
  }

  it should "have fewer false positives than the false positives probability" in {
   val fpProb: Double = 0.04
   val falsePositives = for {
     i <- connectivesFalseWords
     if connectivesBF.contains(i).isTrue
   } yield i
   assert(falsePositives.size <= connectives.size * fpProb)
  }
  
  "A words BloomFilter" should "have 0 false negatives" in {
   for(i <- wordsTestWords) { assert(wordsBF.contains(i).isTrue)}
  }

  it should "have fewer false positives than the false positives probability" in {
   val falsePositives = for {
     i <- wordsFalseWords
     if wordsBF.contains(i).isTrue
   } yield i
   assert(falsePositives.size < words.size * fpProb)
  }
}
