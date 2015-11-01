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
    "estimate total number of integers" in {
      val aa = new HyperLogLogAgent[Int]("test Ints")
      val updateFuture = aa.update(ints)
      whenReady(updateFuture, timeout) { result =>
        result.estimatedSize should equal(ints.size.toDouble +- 0.09)
      }
    }
  }
  
  "HyperLogLogAgent of Longs" should {
    "estimate total number of longs"  in {
      val aa = new HyperLogLogAgent[Long]("test Longs")
      val updateFuture = aa.update(longs)
      whenReady(updateFuture, timeout) { result =>
        result.estimatedSize should equal(longs.size.toDouble +- 0.09)
      }
    }
  }
}
