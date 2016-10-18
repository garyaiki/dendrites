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
import com.twitter.algebird.{CMS, CMSHasher, CMSMonoid}

import com.github.garyaiki.dendrites.algebird.agent.stream.CountMinSketchAgentFlow;
import com.twitter.algebird.CMSHasherImplicits._

import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.github.garyaiki.dendrites.algebird.createCMSMonoid
import com.github.garyaiki.dendrites.algebird.agent.CountMinSketchAgent
import com.github.garyaiki.dendrites.algebird.stream.CreateCMSFlow
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder

/**
  * @author Gary Struthers
  *
  */
class CountMinSketchAgentFlowSpec extends WordSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val m = createCMSMonoid[Long]()
  val timeout = Timeout(3000 millis)

  "An CountMinSketchAgentFlow of Longs" should {
    "update its total count" in {
      val cmsFlow = new CreateCMSFlow[Long]()
      val cmsAgt = new CountMinSketchAgent[Long]("test Longs")
      val cmsAgtFlow = new CountMinSketchAgentFlow(cmsAgt)
      val (pub, sub) = TestSource.probe[Seq[Long]]
        .via(cmsFlow)
        .via(cmsAgtFlow)
        .toMat(TestSink.probe[Future[CMS[Long]]])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(longs)
      val updateFuture = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      whenReady(updateFuture, timeout) { result => result.totalCount should equal(longs.size) }
    }
  }

  "A composite CountMinSketchAgentFlow of Longs" should {
    "update its total count" in {
      val cmsAgt = new CountMinSketchAgent[Long]("test Longs")
      val composite = CountMinSketchAgentFlow.compositeFlow[Long](cmsAgt)
      val (pub, sub) = TestSource.probe[Seq[Long]]
        .via(composite)
        .toMat(TestSink.probe[Future[CMS[Long]]])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(longs)
      val updateFuture = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      whenReady(updateFuture, timeout) {
        result => result.totalCount should equal(longs.size)
        val agentValue = cmsAgt.agent.get()
        agentValue.totalCount should equal(longs.size)
      }
    }
  }

  "A composite sink CountMinSketchAgentFlow of Longs" should {
    "update its total count" in {
      val source = Source.single(longs)
      val cmsAgt = new CountMinSketchAgent[Long]("test Longs")
      val composite = CountMinSketchAgentFlow.compositeSink[Long](cmsAgt)
      source.runWith(composite)
      Thread.sleep(10)//Stream completes before agent updates
      val updateFuture = cmsAgt.agent.future()

      whenReady(updateFuture, timeout) { result =>
        result.totalCount should equal(longs.size)
      }
    }
  }
}
