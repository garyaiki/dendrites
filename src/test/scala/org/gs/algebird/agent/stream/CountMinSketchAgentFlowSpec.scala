/**
  */
package org.gs.algebird.agent.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Keep }
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import com.twitter.algebird.{CMS, CMSHasher, CMSMonoid}
import com.twitter.algebird.CMSHasherImplicits._
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.gs.algebird.createCMSMonoid
import org.gs.algebird.agent.CountMinSketchAgent
import org.gs.algebird.stream.CreateCMSFlow
import org.gs.fixtures.TestValuesBuilder

/**
  * @author Gary Struthers
  *
  */
class CountMinSketchAgentFlowSpec extends FlatSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val m = createCMSMonoid[Long]()
  val timeout = Timeout(3000 millis)

  "An CountMinSketchAgentFlow of Longs" should "update its total count" in {
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
