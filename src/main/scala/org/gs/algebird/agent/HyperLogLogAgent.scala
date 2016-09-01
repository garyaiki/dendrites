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

import akka.agent.Agent
import com.twitter.algebird.{HLL, HyperLogLogAggregator, HyperLogLogMonoid}
import scala.concurrent.{ExecutionContext, Future}

/** Akka Agent for concurrently updating DecayedValues
  *
  * @param name
  * @param xs optional initial values
  * @param ec execution context for future
  * @param monoid HyperLogLogMonoid for zero value and adding HLLs, 12 bits for 1% accuracy
  * @param agg HyperLogLogAggregator to add HLLs, 12 bits for 1% accuracy
  *
  * @example [[org.gs.algebird.agent.stream.HyperLogLogAgentFlow]]
  * @see [[http://doc.akka.io/api/akka/current/#akka.agent.Agent Agent]]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL HLL]]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HyperLogLogAggregator HyperLogLogAggregator]]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HyperLogLogMonoid HyperLogLogMonoid]]
  * @author Gary Struthers
  */
class HyperLogLogAgent(val name: String = "", init: Option[HLL] = None)
  (implicit ec: ExecutionContext, monoid: HyperLogLogMonoid, agg: HyperLogLogAggregator) {

  val agent = init match {
    case None => Agent(monoid.zero)
    case Some(hll) => Agent(hll)
  }

  /** Update agent with sequence of values
    *
    * @param hll HyperLogLog
    * @return future of new HLL after this and all pending updates
    */
  def alter(hll: HLL): Future[HLL] = {
    agent alter (oldState => oldState + hll)
  }
}
