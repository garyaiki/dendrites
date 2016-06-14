package org.gs.algebird.agent

import akka.agent.Agent
import com.twitter.algebird.{DecayedValue, DecayedValueMonoid}
import scala.concurrent.{ExecutionContext, Future}
import org.gs.algebird.toDecayedValues

/** Akka Agent for concurrently updating DecayedValues
  *
  * @see [[http://doc.akka.io/api/akka/current/#akka.agent.Agent Agent]]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.DecayedValue DecayedValue]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.DecayedValueMonoid DecayedValueMonoid]]
  * @author Gary Struthers
  *
  * @param name
  * @param halfLife to scale value based on time
  * @param last is initial element, if None use implicit monoid.zero
  * @param implicit ec execution context for future
  * @param implicit monoid DecayedValueMonoid to scan from initial value
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
