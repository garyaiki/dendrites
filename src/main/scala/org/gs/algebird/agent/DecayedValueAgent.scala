package org.gs.algebird.agent

import akka.agent.Agent
import com.twitter.algebird._
import org.gs.algebird._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.reflect.runtime.universe._

/** Shared state for DecayedValues
  *
  * @see [[http://doc.akka.io/api/akka/current/#akka.agent.Agent]]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.DecayedValue]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.DecayedValueMonoid]]
  * @example [[org.gs.algebird.agent.DecayedValueAgentSpec]]
  * @author garystruthers
  *
  * @param name
  * @param halfLife to scale value based on time
  * @param last is initial element, if None use implicit monoid.zero
  * @param ec execution context for future
  * @param monoid implicit DecayedValueMonoid to scan from initial value
  */
class DecayedValueAgent(val name: String = "", halfLife: Double, last: Option[DecayedValue] = None)
        (implicit ec: ExecutionContext, monoid: DecayedValueMonoid) {
  val agent = last match {
    case None    => Agent(Seq(monoid.zero))
    case Some(x) => Agent(Seq(x))
  }

  /** Update agent with sequence of numeric values
    *
    * @param xs Seq tuple of values and times
    * @return future of new value after this and all pending updates
    */
  def update(xs: Seq[(Double, Double)]): Future[Seq[DecayedValue]] = {
    agent alter (oldState => {
      val decayed = toDecayedValues(halfLife, Some(oldState.last))(xs)
      val mustDrop = (oldState.size + xs.size) - Int.MaxValue
      if (mustDrop > 0) oldState.drop(mustDrop) ++ decayed else oldState ++ decayed
    })
  }

  /** Get then remove oldest values from agent
    *
    * @param length number to remove
    * @return oldest values Not the latest values
    */
  def getOld(length: Int): Seq[DecayedValue] = {
    val oldest = agent.get().take(length)
    agent send (oldState => {
      oldState.drop(length)
    })
    oldest
  }
}
