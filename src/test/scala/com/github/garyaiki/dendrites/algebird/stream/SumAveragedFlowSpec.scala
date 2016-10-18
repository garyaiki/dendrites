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
import akka.stream.scaladsl.{Flow, Keep}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import com.twitter.algebird._
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import com.github.garyaiki.dendrites.aggregator._
import com.github.garyaiki.dendrites.algebird._
import com.github.garyaiki.dendrites.algebird.stream._
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder

/**
  * @author Gary Struthers
  */
class SumAveragedFlowSpec extends WordSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

   
  "A sum of AveragedValues" should {
    "be near the sum of their means" in {
    	val (pub, sub) = TestSource.probe[Seq[BigDecimal]]
    			.via(avgBDFlow.grouped(2))
    			.via(sumAvgFlow)
    			.toMat(TestSink.probe[AveragedValue])(Keep.both)
    			.run()
    			sub.request(2)
    			pub.sendNext(bigDecimals)
    			pub.sendNext(bigDecimals2)
    			val avBD = sub.expectNext()
    			pub.sendComplete()
    			sub.expectComplete()
    			avBD.count shouldBe bigDecimals.size + bigDecimals2.size
    			val mBD = mean(bigDecimals ++ bigDecimals2)
    			avBD.value shouldBe mBD.right.get.toDouble +- 0.005
    }
  }
}
