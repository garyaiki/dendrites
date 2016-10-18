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
package com.github.garyaiki.dendrites.algebird.stream

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.twitter.algebird._
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.Matchers._
import com.github.garyaiki.dendrites.algebird.fixtures.QTreeBuilder

/**
  * @see [[http://en.wikipedia.org/wiki/Interquartile_mean Interquartile_mean]]
  * @see [[http://en.wikipedia.org/wiki/Interquartile_range Interquartile_range]]
  * @author Gary Struthers
  */
class QTreeFlowSpec extends WordSpecLike with QTreeBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  def sourceBD = TestSource.probe[QTree[BigDecimal]]
  def sourceBI = TestSource.probe[QTree[BigInt]]
  def sourceD = TestSource.probe[QTree[Double]]
  def sourceF = TestSource.probe[QTree[Float]]
  def sourceI = TestSource.probe[QTree[Int]]
  def sourceL = TestSource.probe[QTree[Long]]

  def sinkD = TestSink.probe[Double]
  def sinkDD = TestSink.probe[(Double, Double)]

  "A QTree[BigDecimal] Flow" should {
    "return its minimum" in {
    	val (pub, sub) = sourceBD
    			.via(qTreeMinFlow)
    			.toMat(sinkD)(Keep.both)
    			.run()
    			sub.request(1)
    			pub.sendNext(qtBD)
    			val min = sub.expectNext()
    			pub.sendComplete()
    			sub.expectComplete()
    			assert(min <= bigDecimals.min)
    }
  }
  
  "A QTree[BigInt] Flow" should {
    "return its maximum" in {
    	val (pub, sub) = sourceBI
    			.via(qTreeMaxFlow)
    			.toMat(sinkD)(Keep.both)
    			.run()
    			sub.request(1)
    			pub.sendNext(qtBI)
    			val max = sub.expectNext()
    			pub.sendComplete()
    			sub.expectComplete()
    			max should be >= bigInts.max.toDouble
    }
  }
  
  "A QTree[Double] Flow" should {
    "return its 1st quartile bounds" in {
    	val (pub, sub) = sourceD
    			.via(firstQuartileFlow)
    			.toMat(sinkDD)(Keep.both)
    			.run()
    			sub.request(1)
    			pub.sendNext(qtD)
    			val qB = sub.expectNext()
    			pub.sendComplete()
    			sub.expectComplete()
    			qB._1 should be >= 110.0
    			qB._2 should be <= 110 + 0.0001
    }
  }
  
  "A QTree[Float] Flow" should {
    "return its second quartile bounds" in {
    	val (pub, sub) = sourceF
    			.via(secondQuartileFlow)
    			.toMat(sinkDD)(Keep.both)
    			.run()
    			sub.request(1)
    			pub.sendNext(qtF)
    			val qB = sub.expectNext()
    			pub.sendComplete()
    			sub.expectComplete()
    			qB._1 should be >= 121.0
    			qB._2 should be <= 121 + 0.001
    }
  }
  
  "A QTree[Int] Flow" should {
    "return its third quartile bounds" in {
    	val (pub, sub) = sourceI
    			.via(thirdQuartileFlow)
    			.toMat(sinkDD)(Keep.both)
    			.run()
    			sub.request(1)
    			pub.sendNext(qtI)
    			val qB = sub.expectNext()
    			pub.sendComplete()
    			sub.expectComplete()
    			qB._1 should be >= 116.0
    			qB._2 should be <= 116.0 + 0.0001
    }
  }
  
  "A QTree[Long] Flow" should {
    "return its inter quartile mean" in {
    	val (pub, sub) = sourceL
    			.via(interQuartileMeanLFlow)
    			.toMat(sinkDD)(Keep.both)
    			.run()
    			sub.request(1)
    			pub.sendNext(qtL)
    			val iqm = sub.expectNext()
    			pub.sendComplete()
    			sub.expectComplete()
    			iqm._1 should be > 101.2
    			iqm._2 should be < 119.93
    }
  }
}
