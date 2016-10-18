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
package com.github.garyaiki.dendrites.algebird.agent.stream

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.twitter.algebird.{QTree, QTreeSemigroup}

import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.github.garyaiki.dendrites.aggregator.mean
import com.github.garyaiki.dendrites.algebird.{AlgebirdConfigurer, BigDecimalField}
import com.github.garyaiki.dendrites.algebird.agent.QTreeAgent
import com.github.garyaiki.dendrites.algebird.typeclasses.QTreeLike
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder

import com.github.garyaiki.dendrites.algebird.agent.stream.QTreeAgentFlow;

/**
  * @author Gary Struthers
  *
  */
class QTreeAgentFlowSpec extends WordSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val level = AlgebirdConfigurer.qTreeLevel
  implicit val qtBDSemigroup = new QTreeSemigroup[BigDecimal](level)
  val timeout = Timeout(3000 millis)

  "A QTreeAgentFlow of BigDecimals" should {
	  val qtAgt = new QTreeAgent[BigDecimal]("test BigDecimals")
	  val qtAgtFlow = new QTreeAgentFlow(qtAgt)
	  val (pub, sub) = TestSource.probe[Seq[BigDecimal]]
			  .via(qtAgtFlow)
			  .toMat(TestSink.probe[Future[QTree[BigDecimal]]])(Keep.both)
			  .run()
			  sub.request(1)
			  pub.sendNext(bigDecimals)
			  val updateFuture = sub.expectNext()
			  pub.sendComplete()
			  sub.expectComplete()
			  "update its count" in {
		      whenReady(updateFuture, timeout) { result => result.count shouldBe bigDecimals.size }
	  }

	  "update its lower bound" in {
		  whenReady(updateFuture, timeout) { result => assert(result.lowerBound <= bigDecimals.min) }
	  }

	  "update its upper bound" in {
		  whenReady(updateFuture, timeout) { result => assert(result.upperBound >= bigDecimals.max) }
	  }

	  "update its 1st quantile bound" in {
		  whenReady(updateFuture, timeout) { result =>
		    val fstQB = result.quantileBounds(0.25)
		    val q1 = 103.0
		    fstQB._1 should be >= q1
		    fstQB._2 should be <= q1 + 0.0001
		  }
	  }

	  "update its 2nd quantile bound" in {
		  whenReady(updateFuture, timeout) { result =>
		    val sndQB = result.quantileBounds(0.5)
		    val q2 = 110.0
		    sndQB._1 should be >= q2
		    sndQB._2 should be <= q2 + 0.0001
		  }
	  }

	  "update its 3rd quantile bound" in {
		  whenReady(updateFuture, timeout) { result =>
		    val trdQB = result.quantileBounds(0.75)
		    val q3 = 116.0
		    trdQB._1 should be >= q3
		    trdQB._2 should be <= q3 + 0.0001
		  }
	  }
	  "update its range sum bounds" in {
		  whenReady(updateFuture, timeout) { result =>
		    val rsb = result.rangeSumBounds(result.lowerBound, result.upperBound)
		    rsb shouldBe (bigDecimals.sum, bigDecimals.sum)
		  }
	  }

	  "update its range count bounds" in {
		  whenReady(updateFuture, timeout) { result =>
		  val rcb = result.rangeCountBounds(result.lowerBound, result.upperBound)
		  rcb shouldBe (bigDecimals.size, bigDecimals.size)
		  }
	  }
  }


  "A composite sink of QTreeAgentFlow of BigDecimals" should {
    "update its count" in {
      val source = Source.single(bigDecimals)
	    val qtAgent = new QTreeAgent[BigDecimal]("test BigDecimals")
		  val composite = QTreeAgentFlow.compositeSink[BigDecimal](qtAgent)
	    source.runWith(composite)
      Thread.sleep(10)//Stream completes before agent updates
		  val updateFuture = qtAgent.agent.future()
		  whenReady(updateFuture, timeout) { result =>
		    result.count shouldBe bigDecimals.size
		  }
	  }
	}
}
