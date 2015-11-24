package org.gs.algebird

import com.twitter.algebird.{DecayedValueMonoid, HyperLogLogAggregator, HyperLogLogMonoid}
import org.scalatest.{ WordSpecLike, Matchers }
import org.scalatest._
import org.scalatest.Matchers._

class AlgebirdConfigurerSpec extends WordSpecLike {
  "An AlgebirdConfigurer CountMinSketch" should {
    "have a delta" in {
      AlgebirdConfigurer.countMinSketchDelta should equal(1E-10)
    }
    "have an EPS" in {
      AlgebirdConfigurer.countMinSketchEPS should equal(0.001)
    }
    "have a SEED" in {
      AlgebirdConfigurer.countMinSketchSeed should equal(1)
    }
  }
  "An AlgebirdConfigurer DecayedValue" should {
    "have an epsilon" in {
      AlgebirdConfigurer.decayedValueEpsilon should equal(0.001)
    }
    "have a halfLife" in {
      AlgebirdConfigurer.decayedValueHalfLife should equal(10.0)
    }
    "have a monoid" in {
      AlgebirdConfigurer.decayedValueMonoid shouldBe an [DecayedValueMonoid]
    }
  }
  "An AlgebirdConfigurer HyperLogLog" should {
    "have bits" in {
      AlgebirdConfigurer.hyperLogLogBits should equal(12)
    }
    "have an aggregator" in {
      AlgebirdConfigurer.hyperLogLogAgggregator shouldBe an [HyperLogLogAggregator]
    }
    "have a monoid" in {
      AlgebirdConfigurer.hyperLogLogMonoid shouldBe an [HyperLogLogMonoid]
    }
  }
  "An AlgebirdConfigurer QTree" should {
    "have a level" in {
      AlgebirdConfigurer.qTreeLevel should equal(16)
    }
  }
}
