/**
  */
package org.gs.algebird.agent.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Keep }
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import com.twitter.algebird.AveragedValue
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.gs.aggregator.mean
import org.gs.algebird.avg
import org.gs.algebird.agent.AveragedAgent
import org.gs.algebird.stream.avgFlow
import org.gs.fixtures.TestValuesBuilder

/**
  * @author Gary Struthers
  *
  */
class AveragedAgentFlowSpec extends WordSpecLike with TestValuesBuilder {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  val timeout = Timeout(3000 millis)

  "An AveragedAgentFlow of BigDecimals" should {
    "update its AveragedValue" in {
      val avgAgt = new AveragedAgent("test BigDecimals")
      val avgAgtFlow = new AveragedAgentFlow(avgAgt)
      val (pub, sub) = TestSource.probe[Seq[BigDecimal]]
        .via(avgFlow)
        .via(avgAgtFlow)
        .toMat(TestSink.probe[Future[AveragedValue]])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(bigDecimals)
      val updateFuture = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      val mBD = mean(bigDecimals)
      whenReady(updateFuture, timeout) { result =>
          result should equal(avg(bigDecimals))
          assert(result.value === (mBD.right.get.toDouble +- 0.005))
      }
    }
  }

  "An AveragedAgentFlow of Doubles" should {
    "update its AveragedValue" in {
      val avgAgt = new AveragedAgent("test Doubles")
      val avgAgtFlow = new AveragedAgentFlow(avgAgt)
      val (pub, sub) = TestSource.probe[Seq[Double]]
        .via(avgFlow)
        .via(avgAgtFlow)
        .toMat(TestSink.probe[Future[AveragedValue]])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(doubles)
      val updateFuture = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      val mD = mean(doubles)
      whenReady(updateFuture, timeout) { result =>
        result should equal(avg(doubles))
        assert(result.value === (mD.right.get +- 0.005))
      }
    }
  }

  "An AveragedAgentFlow of Floats" should {
    "update its AveragedValue" in {
      val avgAgt = new AveragedAgent("test Floats")
      val avgAgtFlow = new AveragedAgentFlow(avgAgt)
      val (pub, sub) = TestSource.probe[Seq[Float]]
        .via(avgFlow)
        .via(avgAgtFlow)
        .toMat(TestSink.probe[Future[AveragedValue]])(Keep.both)
        .run()
      sub.request(1)
      pub.sendNext(floats)
      val updateFuture = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()
      val mF = mean(floats)
      whenReady(updateFuture, timeout) { result =>
        result should equal(avg(floats))
        assert(result.value === (mF.right.get.toDouble +- 0.005))
      }
    }
  }
}
