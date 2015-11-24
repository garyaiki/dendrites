package org.gs.algebird

import com.typesafe.config.{ Config, ConfigFactory }

import com.twitter.algebird.{DecayedValueMonoid, HyperLogLogAggregator, HyperLogLogMonoid}

object AlgebirdConfigurer {
  val config = ConfigFactory.load()
  val countMinSketchDelta = config.getNumber("dendrites.algebird.countMinSketch.delta")
  val countMinSketchEPS = config.getDouble("dendrites.algebird.countMinSketch.eps")
  val countMinSketchSeed = config.getInt("dendrites.algebird.countMinSketch.seed")
  val decayedValueEpsilon = config.getDouble("dendrites.algebird.decayedValue.epsilon")
  val decayedValueMonoid = DecayedValueMonoid(decayedValueEpsilon)
  val decayedValueHalfLife = config.getDouble("dendrites.algebird.decayedValue.halfLife")
  val hyperLogLogBits = config.getInt("dendrites.algebird.hyperLogLog.bits")
  val hyperLogLogAgggregator = HyperLogLogAggregator(hyperLogLogBits)
  val hyperLogLogMonoid = new HyperLogLogMonoid(hyperLogLogBits)
  val qTreeLevel = config.getInt("dendrites.algebird.qTree.level")
}