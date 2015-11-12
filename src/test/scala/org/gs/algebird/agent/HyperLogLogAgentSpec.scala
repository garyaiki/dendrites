/**
  */
package org.gs.algebird.agent

import com.twitter.algebird._
import org.gs.aggregator._
import org.gs.algebird._
import org.gs.fixtures.TestValuesBuilder
import org.scalatest.{ Matchers, WordSpecLike }
import org.scalatest._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global

/** @author garystruthers
  *
  */
class HyperLogLogAgentSpec extends WordSpecLike with Matchers with TestValuesBuilder {
  implicit val ag = HyperLogLogAggregator(12)
  implicit val monoid = new HyperLogLogMonoid(12)

  val timeout = Timeout(3000 millis)
  
  "HyperLogLogAgent of Ints" should {
    "estimate number of distinct integers" in {
      val aa = new HyperLogLogAgent("test Ints")
      val hll = createHLL(ints)
      val updateFuture = aa.update(hll)
      whenReady(updateFuture, timeout) { result =>
        result.estimatedSize should equal(ints.distinct.size.toDouble +- 0.09)
      }
    }
  }
  
  "HyperLogLogAgent of Longs" should {
    "estimate number of distinct longs"  in {
      val aa = new HyperLogLogAgent("test Longs")
      val hll = createHLL(longs)
      val updateFuture = aa.update(hll)
      whenReady(updateFuture, timeout) { result =>
        result.estimatedSize should equal(longs.distinct.size.toDouble +- 0.09)
      }
    }
  }
}
