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

/** Akka [[http://doc.akka.io/api/akka/current/akka/agent/index.html Agent]] for concurrently updating and
  * accessing [[http://twitter.github.io/algebird/ Algebird]] approximate data structures
  *
  * == AveragedAgent ==
  * Create AveragedValue Agent, update it, Future will have latest AveragedValue
  * {{{
  * val aa = new AveragedAgent("test BigDecimals")
  * val another = avg(bigDecimals)
  * val updateFuture: Future[AveragedValue] = aa.alter(another)
  * }}}
  *
  * == CountMinSketchAgent ==
  * Create CountMinSketch Agent, update it, Future will have latest CMS value
  * {{{
  * val cmsAgt = new CountMinSketchAgent[Long]("test Longs")
  * val cms0 = createCountMinSketch(longs)
  * val updateFuture: Future[CMS[Long]] = cmsAgt.alter(cms0)
  * }}}
  *
  * == DecayedValueAgent ==
  * Decayed value diminishes with time, values are paired with a time
  * {{{
  * implicit val m = DecayedValueMonoid(0.001)
  * val timeout = Timeout(3000 millis)
  * val sines = genSineWave(100, 0 to 360) // dummy data
  * val days = Range.Double(0.0, 361.0, 1.0) // dummy times as Double
  * val sinesZip = sines.zip(days)
  * }}}
  * Create DecayedValue Agent, update it, Future will have latest sequence of DecayedValue
  * {{{
  * val decayedValues = new DecayedValueAgent("test90", 10.0, None)
  * val updateFuture: Future[Seq[DecayedValue]] = decayedValues.alter(sinesZip)
  * }}}
  *
  * == HyperLogLogAgent ==
  * Configure HyperLogLogAggregator and HyperLogLogMonoid
  * {{{
  * implicit val ag = AlgebirdConfigurer.hyperLogLogAgggregator
  * implicit val monoid = AlgebirdConfigurer.hyperLogLogMonoid
  * }}
  * Create HyperLogLog Agent, update it, Future will have latest HLL
  * {{{
  * val aa = new HyperLogLogAgent("test Ints")
  * val hll = createHLL(ints)
  * val updateFuture: Future[HLL] = aa.alter(hll)
  * }}}
  *
  * == QTreeAgent ==
  * Configure level and create QTreeSemigroup
  * {{{
  * val level = AlgebirdConfigurer.qTreeLevel
  * implicit val qtBDSemigroup = new QTreeSemigroup[BigDecimal](level)
  * }}}
  * Create QTree Agent, update it, Future will have latest QTree
  * {{{
  * val qTreeAgent = new QTreeAgent[BigDecimal]("testBD 1st update data")
  * val updateFuture: Future[QTree[BigDecimal]] = qTreeAgent.alter(bigDecimals)
  * }}}
  *
  * @author Gary Struthers
  *
  */
package object agent
