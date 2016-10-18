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
package com.github.garyaiki.dendrites.examples.account.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

/** Server for integration tests under `com.github.garyaiki.dendrites.examples`
  * @note Sometimes tests fail when run together, this seems to be a Scalatest issue, rerun
  * failed test individually.
  * @see [[com.github.garyaiki.dendrites.examples.account.http.BalancesService]]
  *
  * In a terminal window
  * {{{
  * $ sbt
  * ...
  * > run
  * }}}
  * In another terminal window
  * {{{
  * > it:testOnly com.github.garyaiki.dendrites.examples.*
  * ...
  * [info] ScalaTest
  * [info] Run completed in 7 seconds, 744 milliseconds.
  * [info] Total number of tests run: 75
  * [info] Suites: completed 17, aborted 0
  * [info] Tests: succeeded 75, failed 0, canceled 0, ignored 0, pending 0
  * [info] All tests passed.
  * [info] Passed: Total 75, Failed 0, Errors 0, Passed 75
  * [success] Total time: 10 s, completed Sep 2, 2016 12:32:55 PM
  * }}}
  *
  *  @author Gary Struthers
  *
  */
object BalancesServer extends App with BalancesService {
  override implicit val system = ActorSystem("dendrites")
  override implicit val executor = system.dispatcher
  override implicit val mat = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("balancesIP"), config.getInt("balancesPort"))
}
