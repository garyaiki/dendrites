package org.gs.algebird.agent

import akka.agent.Agent
import com.twitter.algebird.{AveragedGroup, AveragedValue}
import scala.concurrent.{ExecutionContext, Future}

/** Akka Agent for concurrently updating AveragedValue
  *
  * @see [[http://doc.akka.io/api/akka/current/#akka.agent.Agent]]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue]
  * @example [[org.gs.algebird.agent.AveragedAgentSpec]]
  * @author garystruthers
  *
  * @param name
  * @param init optional initial AveragedValue
  * @param ec execution context for future
  */
class AveragedAgent(val name: String = "", init: AveragedValue = new AveragedValue(0, 0.0))
  (implicit ec: ExecutionContext) {

  val agent = Agent(init)

  /** Update agent with another AveragedValue value
    *
    * @param another AveragedValue
    * @return future of new value for this and all pending updates
    */
  def alter(avg: AveragedValue): Future[AveragedValue] = {
    agent alter (oldState => {
      AveragedGroup.plus(oldState, avg)
    })
  }
}
