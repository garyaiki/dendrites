/**
  */
package com.github.garyaiki.dendrites.algebird.agent

import com.twitter.algebird.{HyperLogLogAggregator, HyperLogLogMonoid}
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.garyaiki.dendrites.algebird.{AlgebirdConfigurer, createHLL}
import com.github.garyaiki.dendrites.fixtures.TestValuesBuilder

/**
  *
  * @author Gary Struthers
  *
  */
class HyperLogLogAgentSpec extends WordSpecLike with Matchers with TestValuesBuilder {
  implicit val ag = AlgebirdConfigurer.hyperLogLogAgggregator
  implicit val monoid = AlgebirdConfigurer.hyperLogLogMonoid

  val timeout = Timeout(3000 millis)
  
  "HyperLogLogAgent of Ints" should {
    "estimate number of distinct integers" in {
      val aa = new HyperLogLogAgent("test Ints")
      val hll = createHLL(ints)
      val updateFuture = aa.alter(hll)
      whenReady(updateFuture, timeout) { result =>
        result.estimatedSize should equal(ints.distinct.size.toDouble +- 0.09)
      }
    }
  }
  
  "HyperLogLogAgent of Longs" should {
    "estimate number of distinct longs"  in {
      val aa = new HyperLogLogAgent("test Longs")
      val hll = createHLL(longs)
      val updateFuture = aa.alter(hll)
      whenReady(updateFuture, timeout) { result =>
        result.estimatedSize should equal(longs.distinct.size.toDouble +- 0.09)
      }
    }
  }
}
