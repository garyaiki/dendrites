package org.gs.algebird.agent

import akka.agent.Agent
import com.twitter.algebird._
import org.gs.algebird._
import org.gs.algebird.typeclasses.HyperLogLogLike
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.reflect.runtime.universe._

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
class HyperLogLogAgent[A: HyperLogLogLike](val name: String = "", xs: Option[Seq[A]] = None)
          (implicit ec: ExecutionContext, monoid: HyperLogLogMonoid, agg: HyperLogLogAggregator) {
  val agent = xs match {
    case None    => Agent(monoid.zero)
    case Some(xs) => Agent(createHLL(xs))
  }

  /** Update agent with sequence of values
    *
    * @param xs Seq of Int or Long values
    * @return future of new HLL after this and all pending updates
    */
  def update(xs: Seq[A]): Future[HLL] = {
    agent alter (oldState => {
      oldState + createHLL(xs)
    })
  }
}
