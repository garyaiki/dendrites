/**
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
package com.github.garyaiki.dendrites.algebird

import com.twitter.algebird.{DecayedValueMonoid, HyperLogLogAggregator, HyperLogLogMonoid}
import com.typesafe.config.{Config, ConfigFactory}

/** Factory for Algebird configured constants
  *
  * @author Gary Struthers
  *
  */
object AlgebirdConfigurer {
  val config = ConfigFactory.load
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
