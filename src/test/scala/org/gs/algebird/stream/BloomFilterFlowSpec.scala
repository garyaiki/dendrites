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
package org.gs.algebird.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.twitter.algebird._
import scala.concurrent.duration._
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import org.gs.algebird.fixtures.BloomFilterBuilder

/** Algebird Bloom Filter tests
 	* @Note BloomFilterBuilder reads OSX dictionaries, you MUST modify for other OS's
 	*
 	* @author Gary Struthers
  *
  */
class BloomFilterFlowSpec extends WordSpecLike with BloomFilterBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  val properFilter: Flow[String, String, NotUsed] =
    Flow[String].filter { x => properBF.contains(x).isTrue }

  val properMat = TestSource.probe[String]
    .via(properFilter)
    .toMat(TestSink.probe[String])(Keep.both)

  "A properNames BloomFilter Flow" should {
    "have 0 false negatives" in {
    	val (pub, sub) = properMat.run()
    			sub.request(properTestWords.size)
    			for (i <- properTestWords) {
    				pub.sendNext(i)
    				sub.expectNext(i)
    			}
    	pub.sendComplete()
    	sub.expectComplete()
    }
  }

  it should {
    "have fewer false positives than the false positives probability" in {
    	val fpProb: Double = 0.02
    			val (pub, sub) = properMat.run()
    			sub.request(properFalseWords.size)

    			for (i <- properFalseWords) pub.sendNext(i)

    			val falsePositives = sub.receiveWithin(FiniteDuration(1, SECONDS), properFalseWords.size)
    			falsePositives.size <= properNames.size * fpProb
    }
  }

  val connectivesFilter: Flow[String, String, NotUsed] =
    Flow[String].filter { x => connectivesBF.contains(x).isTrue }

  val connectivesMat = TestSource.probe[String]
    .via(connectivesFilter)
    .toMat(TestSink.probe[String])(Keep.both)

  "A connectives BloomFilter Flow" should {
    "have 0 false negatives" in {
    	val (pub, sub) = connectivesMat.run()
    			sub.request(connectivesTestWords.size)
    			for (i <- connectivesTestWords) {
    				pub.sendNext(i)
    				sub.expectNext(i)
    			}
    	pub.sendComplete()
    	sub.expectComplete()
    }
  }

  it should {
    "have fewer false positives than the false positives probability" in {
    val fpProb: Double = 0.04
    val (pub, sub) = connectivesMat.run()
    sub.request(connectivesFalseWords.size)

    for (i <- connectivesFalseWords) pub.sendNext(i)
    val falsePositives = sub.receiveWithin(FiniteDuration(1, SECONDS), connectivesFalseWords.size)
    falsePositives.size <= connectives.size * fpProb
    }
  }

  val wordsFilter: Flow[String, String, NotUsed] =
    Flow[String].filter { x => wordsBF.contains(x).isTrue }

  val wordsMat = TestSource.probe[String]
    .via(wordsFilter)
    .toMat(TestSink.probe[String])(Keep.both)

  "A words BloomFilter Flow" should {
    "have 0 false negatives" in {
    	val (pub, sub) = wordsMat.run()
    			sub.request(wordsTestWords.size)
    			for (i <- wordsTestWords) {
    				pub.sendNext(i)
    				sub.expectNext(i)
    			}
    	pub.sendComplete()
    	sub.expectComplete()
    }
  }

  it should {
    "have fewer false positives than the false positives probability" in {
    val fpProb: Double = 0.04
    val (pub, sub) = wordsMat.run()
    sub.request(wordsFalseWords.size)
        for (i <- wordsFalseWords) pub.sendNext(i)
    val falsePositives = sub.receiveWithin(FiniteDuration(1, SECONDS), wordsFalseWords.size)
    falsePositives.size < words.size * fpProb
    }
  }
}
