package org.gs.algebird.agent

import akka.agent.Agent
import com.twitter.algebird._
import org.gs.algebird._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.reflect.runtime.universe._

/** Shared state for AveragedValue
  *
  * @see [[http://doc.akka.io/api/akka/current/#akka.agent.Agent]]
  * @see [[http://twitter.github.io/algebird/#com.twitter.algebird.AveragedValue]
  * @example [[org.gs.algebird.agent.AveragedAgentSpec]]
  * @author garystruthers
  *
  */
class AveragedAgent(val name: String = "", init: AveragedValue = new AveragedValue(0, 0.0))(implicit ec: ExecutionContext) {
  val agent = Agent(init)

  /** Update agent with sequence of numeric values
    *
    * @tparam A: Numeric, elements must be Numeric
    * @param xs Seq
    * @param evidence implicit Numeric[A]
    * @return future of new value after this and all pending updates
    */
  def update[A: TypeTag: Numeric](xs: Seq[A]): Future[AveragedValue] = {
    agent alter (oldState => {
      AveragedGroup.plus(oldState, avg[A](xs))
    })
  }
}