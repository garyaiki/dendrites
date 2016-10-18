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
import org.scalatest.{Matchers, WordSpecLike}
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder
import com.github.garyaiki.dendrites.stream.{collectRightFlow, flattenFlow}

/** @author Gary Struthers
  *
  */
class MaxFlowSpec extends WordSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  "A Sequence of BigDecimal" should {
    "return its Max" in {
    	val (pub, sub) = TestSource.probe[Seq[BigDecimal]]
    			.via(maxFlow)
    			.toMat(TestSink.probe[BigDecimal])(Keep.both)
    			.run()
    			sub.request(1)
    			pub.sendNext(bigDecimals)
    			sub.expectNext(bigDecimals.max)
    			pub.sendComplete()
    			sub.expectComplete()
    }
  }

  "A Sequence of String" should {
    "return its Max" in {
    	val (pub, sub) = TestSource.probe[Seq[String]]
    			.via(maxFlow)
    			.toMat(TestSink.probe[String])(Keep.both)
    			.run()
    			sub.request(1)
    			pub.sendNext(strings)
    			sub.expectNext(strings.max)
    			pub.sendComplete()
    			sub.expectComplete()
    }
  }

  "A Sequence of Option[BigInt]" should {
    "return its Max" in {
    	val (pub, sub) = TestSource.probe[Seq[Option[BigInt]]]
    			.via(flattenFlow)
    			.via(maxFlow)
    			.toMat(TestSink.probe[BigInt])(Keep.both)
    			.run()
    			sub.request(1)
    			pub.sendNext(optBigInts)
    			sub.expectNext(optBigInts.flatten.max)
    			pub.sendComplete()
    			sub.expectComplete()
    }
  }

  "A Sequence of Either[String, Double]" should {
    "return its Max" in {
    	val (pub, sub) = TestSource.probe[Seq[Either[String, Double]]]
    			.via(collectRightFlow)
    			.via(maxFlow)
    			.toMat(TestSink.probe[Double])(Keep.both)
    			.run()
    			sub.request(1)
    			pub.sendNext(eithDoubles)
    			sub.expectNext(doubles.max)
    			pub.sendComplete()
    			sub.expectComplete()
    }
  }
}
