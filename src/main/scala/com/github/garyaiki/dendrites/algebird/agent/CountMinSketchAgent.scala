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
import com.twitter.algebird.{CMS, CMSHasher}
import scala.concurrent.{ExecutionContext, Future}
import com.github.garyaiki.dendrites.algebird.createCMSMonoid

/** Akka Agent for concurrently updating CountMinSketch
  *
  * @constructor Creates Agent singleton for CountMinSketch
  * @tparam K elements which are implicitly Ordering[K] and CMSHasher[K]
  * @param name Agent name
  * @param ec implicit ExecutionContext
  *
  * @example [[com.github.garyaiki.dendrites.algebird.agent.stream.CountMinSketchAgentFlow]]
  * @see [[http://doc.akka.io/api/akka/current/akka/agent/index.html Agent]]
  * @see [[http://twitter.github.io/algebird/datatypes/approx/countminsketch.html CMSHasher]]
  * @see [[http://www.scala-lang.org/api/current/scala/math/Ordering.html Ordering]]
  *
  * @author Gary Struthers
  */
class CountMinSketchAgent[K: Ordering: CMSHasher](val name: String = "")(implicit ec: ExecutionContext) {

  val zero = createCMSMonoid[K]().zero
  val agent = Agent(zero)

  /** Update agent with sequence of CMS[K]
    *
    * @param other CMS
    * @return future of combined CMS after this and all pending updates
    */
  def alter(cms: CMS[K]): Future[CMS[K]] = agent alter (oldState => oldState ++ cms)
}
