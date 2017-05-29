/**
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
import com.twitter.algebird.{HLL, HyperLogLogAggregator, HyperLogLogMonoid}
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.github.garyaiki.dendrites.algebird.AlgebirdConfigurer
import com.github.garyaiki.dendrites.algebird.agent.HyperLogLogAgent
import com.github.garyaiki.dendrites.algebird.stream.CreateHLLFlow
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder

/**
  *
  * @author Gary Struthers
  *
  */
class HyperLogLogAgentFlowSpec extends WordSpecLike with Matchers with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val ag = AlgebirdConfigurer.hyperLogLogAgggregator
  implicit val monoid = AlgebirdConfigurer.hyperLogLogMonoid
  val timeout = Timeout(3000 millis)

  "HyperLogLogAgentFlow of Longs" should {
     "update its total count" in {
       val hllFlow = new CreateHLLFlow[Long]()
       val hllAgt = new HyperLogLogAgent("test Longs")
       val hllAgtFlow = new HyperLogLogAgentFlow(hllAgt)
       val (pub, sub) = TestSource.probe[Seq[Long]]
        .via(hllFlow)
        .via(hllAgtFlow)
        .toMat(TestSink.probe[Future[HLL]])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(longs)
      val updateFuture = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      whenReady(updateFuture, timeout) { result =>
        result.estimatedSize should equal(longs.distinct.size.toDouble +- 0.09)
      }
     }
  }

  "A composite HyperLogLogAgentFlow of Longs" should {
     "update its total count" in {
       val hllAgent = new HyperLogLogAgent("test Longs")
       val composite = HyperLogLogAgentFlow.compositeFlow[Long](hllAgent)
       val (pub, sub) = TestSource.probe[Seq[Long]]
        .via(composite)
        .toMat(TestSink.probe[Future[HLL]])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(longs)
      val updateFuture = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      whenReady(updateFuture, timeout) { result =>
        result.estimatedSize should equal(longs.distinct.size.toDouble +- 0.09)
      }
    }
  }

  "A composite sink HyperLogLogAgentFlow of Longs" should {
     "update its total count" in {
       val source = Source.single(longs)
       val hllAgent = new HyperLogLogAgent("test Longs")
       val composite = HyperLogLogAgentFlow.compositeSink[Long](hllAgent)
       source.runWith(composite)
       Thread.sleep(10) // Stream completes before agent updates

      val updateFuture = hllAgent.agent.future()
      whenReady(updateFuture, timeout) { result =>
        result.estimatedSize should equal(longs.distinct.size.toDouble +- 0.09)
      }
    }
  }
}
