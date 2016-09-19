/** Copyright 2016 Gary Struthers

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
package org.gs.algebird.agent

import com.twitter.algebird.{CMSHasher, DecayedValue, DecayedValueMonoid, HLL, QTreeSemigroup}
import scala.concurrent.ExecutionContext
import scala.reflect.runtime.universe.TypeTag
import org.gs.algebird.AlgebirdConfigurer
import org.gs.algebird.typeclasses.{HyperLogLogLike, QTreeLike}


/** Create Agents for Algebird AveragedValue, CountMinSketch, DecayedValue, HyperLogLog, QTree
  *
  * @constructor constructs agents and loads Algebird support classes
  * @tparam A: Numeric type that can be used with all the agents
  * @param name optional agent name
  * @param last optional previous DecayedValue
  * @param init optional initial HLL
  * @param ec implicit ExecutionContext
  * @param qtSG implicitQTreeSemigroup with type A
  * @author Gary Struthers
  */
class Agents[A: CMSHasher: HyperLogLogLike: Numeric: QTreeLike: TypeTag](name: String = "",
        last: Option[DecayedValue] = None,
        init: Option[HLL] = None)(implicit ec: ExecutionContext, qtSG: QTreeSemigroup[A]) {
  val configs = AlgebirdConfigurer
  val avgAgent = new AveragedAgent
  val cmsAgent = new CountMinSketchAgent[A]
  implicit val dcaMonoid = configs.decayedValueMonoid
  val dcaAgent = new DecayedValueAgent(name, configs.decayedValueHalfLife, last)
  implicit val hllAgg = configs.hyperLogLogAgggregator
  implicit val hllMonoid =  configs.hyperLogLogMonoid
  val hllAgent = new HyperLogLogAgent(name, init)
  val qtAgent: QTreeAgent[A] = new QTreeAgent[A]
}
