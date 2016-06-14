/**
  */
package org.gs.algebird.agent.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Keep }
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import com.twitter.algebird.{HLL, HyperLogLogAggregator, HyperLogLogMonoid}
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.gs.algebird.AlgebirdConfigurer
import org.gs.algebird.stream.CreateHLLFlow
import org.gs.algebird.agent.HyperLogLogAgent
import org.gs.fixtures.TestValuesBuilder

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
}
