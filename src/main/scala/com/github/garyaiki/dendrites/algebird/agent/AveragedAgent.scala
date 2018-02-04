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
import com.twitter.algebird.{AveragedGroup, AveragedValue}
import scala.concurrent.{ExecutionContext, Future}

/** Akka Agent for concurrently updating AveragedValue.
  *
  * @deprecated
  * @constructor Creates Agent singleton for AveragedValue
  * @param name
  * @param init optional initial AveragedValue
  * @param ec implicit ExecutionContext
  *
  * @example [[com.github.garyaiki.dendrites.algebird.agent.stream.AveragedAgentFlow]]
  * @see [[http://doc.akka.io/api/akka/current/akka/agent/index.html Agent]]
  * @see [[http://twitter.github.io/algebird/datatypes/averaged_value.html AveragedValue]]
  * @author Gary Struthers
  */
class AveragedAgent(val name: String = "", init: AveragedValue = new AveragedValue(0, 0.0))
  (implicit ec: ExecutionContext) {

  val agent = Agent(init)

  /** Update agent with another AveragedValue value
    *
    * @param another AveragedValue
    * @return future of new value for this and all pending updates
    */
  def alter(avg: AveragedValue): Future[AveragedValue] = agent alter (oldState => AveragedGroup.plus(oldState, avg))
}
