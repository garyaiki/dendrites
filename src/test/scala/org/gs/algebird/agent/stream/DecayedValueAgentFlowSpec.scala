/**
  */
package org.gs.algebird.agent.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Keep }
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import com.twitter.algebird.{DecayedValue, DecayedValueMonoid}
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.gs.algebird.AlgebirdConfigurer
import org.gs.algebird.agent.DecayedValueAgent
import org.gs.algebird.stream.ZipTimeFlow
import org.gs.fixtures.TrigUtils

/**
  * @author Gary Struthers
  *
  */
class DecayedValueAgentFlowSpec extends FlatSpecLike with TrigUtils {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  implicit val m = AlgebirdConfigurer.decayedValueMonoid
  val halfLife = AlgebirdConfigurer.decayedValueHalfLife
  val timeout = Timeout(3000 millis)
  val sines = genSineWave(100, 0 to 360)
  val days = Range.Double(0.0, 361.0, 1.0)
  def nextTime[T](it: Iterator[Double])(x: T): Double = it.next
  val curriedNextTime = nextTime[Double](days.iterator) _
  val meanDay90 = sines.take(90).sum / 90
  val dvAgt = new DecayedValueAgent("test90", halfLife, None)
  val dvAgtFlow = new DecayedValueAgentFlow(dvAgt)
  val (pub, sub) = TestSource.probe[Seq[Double]]
    .via(new ZipTimeFlow[Double](curriedNextTime))
    .via(dvAgtFlow)
    .toMat(TestSink.probe[Future[Seq[DecayedValue]]])(Keep.both)
    .run()
  sub.request(1)
  pub.sendNext(sines)
  val updateFuture = sub.expectNext()
  pub.sendComplete()
  sub.expectComplete()
    
  "A DecayedValueAgentFlow of value/time doubles" should "exceed the mean for 1st 90 values" in {
    whenReady(updateFuture, timeout) { result =>
        assert(result(90).average(halfLife) > meanDay90)
    }
  }

  it should "be less than the mean for the first 180" in {
    whenReady(updateFuture, timeout) { result =>
      assert(result(180).average(halfLife) < sines.take(180).sum / 180)
    }
  }

  it should "be less than the mean for the first 270" in {
    whenReady(updateFuture, timeout) { result =>
      assert(result(270).average(halfLife) < sines.take(270).sum / 270)
    }
  }

  it should "be less than the mean for all 3600" in {
    whenReady(updateFuture, timeout) { result =>
      assert(result(360).average(halfLife) < sines.take(360).sum / 360)
    }
  }
}
