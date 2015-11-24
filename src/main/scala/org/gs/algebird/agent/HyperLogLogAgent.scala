package org.gs.algebird.agent

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import akka.agent.Agent
import com.twitter.algebird.{HLL, HyperLogLogAggregator, HyperLogLogMonoid}

/** Shared state for DecayedValues
  *
  * @see [[http://doc.akka.io/api/akka/current/#akka.agent.Agent]]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HLL]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HyperLogLogAggregator]]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.HyperLogLogMonoid]]
  * @example [[org.gs.algebird.agent.HyperLogLogAgentSpec]]
  * @author garystruthers
  *
  * @tparam A can only be Int or Long HyperLogLogLike typeclass supports this
  * @param name
  * @param xs optional initial values
  * @param ec execution context for future
  * @param monoid HyperLogLogMonoid for zero value and adding HLLs, 12 bits for 1% accuracy
  * @param agg HyperLogLogAggregator to add HLLs, 12 bits for 1% accuracy
  *
  */
class HyperLogLogAgent(val name: String = "", init: Option[HLL] = None)
          (implicit ec: ExecutionContext, monoid: HyperLogLogMonoid, agg: HyperLogLogAggregator) {
  val agent = init match {
    case None    => Agent(monoid.zero)
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
