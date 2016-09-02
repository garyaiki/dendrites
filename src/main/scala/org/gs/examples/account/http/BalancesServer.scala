/**
  */
package org.gs.examples.account.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

/** Server for integration tests under `org.gs.examples`
	* @note Sometimes tests fail when run together, this seems to be a Scalatest issue, rerun
	* failed test individually.
  * @see [[org.gs.examples.account.http.BalancesService]] 
  * 
  * In a terminal window
  * {{{
  * $ sbt
  * ...
  * > run
  * }}}
  *	In another terminal window
  * {{{
  * > it:testOnly org.gs.examples.*
  * ...
  * [info] ScalaTest
	*	[info] Run completed in 7 seconds, 744 milliseconds.
	*	[info] Total number of tests run: 75
	*	[info] Suites: completed 17, aborted 0
	*	[info] Tests: succeeded 75, failed 0, canceled 0, ignored 0, pending 0
	*	[info] All tests passed.
	*	[info] Passed: Total 75, Failed 0, Errors 0, Passed 75
	*	[success] Total time: 10 s, completed Sep 2, 2016 12:32:55 PM
	* }}}
	*
  *  @author Gary Struthers
  *
  */
object BalancesServer extends App with BalancesService {
  override implicit val system = ActorSystem("dendrites")
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("balancesIP"), config.getInt("balancesPort"))
}
