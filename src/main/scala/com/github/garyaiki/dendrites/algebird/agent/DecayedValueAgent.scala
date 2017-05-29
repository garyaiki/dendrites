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
package com.github.garyaiki.dendrites.algebird.agent

import akka.agent.Agent
import com.twitter.algebird.{DecayedValue, DecayedValueMonoid}
import scala.concurrent.{ExecutionContext, Future}
import com.github.garyaiki.dendrites.algebird.toDecayedValues

/** Akka Agent for concurrently updating DecayedValue
  *
  * @constructor Creates Agent singleton for CountMinSketch
  * @param name
  * @param halfLife to scale value based on time
  * @param last is initial element, if None use implicit monoid.zero
  * @param ec implicit execution context for future
  * @param monoid implicit DecayedValueMonoid to scan from initial value
  *
  * @example [[com.github.garyaiki.dendrites.algebird.agent.stream.DecayedValueAgentFlow]]
  * @see [[http://doc.akka.io/api/akka/current/#akka.agent.Agent Agent]]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.DecayedValue DecayedValue]]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.DecayedValueMonoid DecayedValueMonoid]]
  * @author Gary Struthers
  */
class DecayedValueAgent(val name: String = "", halfLife: Double, last: Option[DecayedValue] = None)
  (implicit ec: ExecutionContext, monoid: DecayedValueMonoid) {

  val agent = last match {
    case None => Agent(Seq(monoid.zero))
    case Some(x) => Agent(Seq(x))
  }

  /** Update agent with sequence of value / time doubles
    *
    * @param xs Seq tuple of values and times
    * @return future of new value after this and all pending updates
    */
  def alter(xs: Seq[(Double, Double)]): Future[Seq[DecayedValue]] = {
    agent alter (oldState => {
      val decayed = toDecayedValues(halfLife, Some(oldState.last))(xs)
      oldState ++ decayed
    })
  }
}
