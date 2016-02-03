package org.gs.akka

import akka.actor.ActorSystem
import org.scalatest.WordSpecLike
import org.scalatest._
import org.scalatest.Matchers._

class DispatchersSpec extends WordSpecLike {
  implicit val system = ActorSystem("dendrites")
  "An ActorSystem" should {
    "have the configured system name" in {
      assert(system.name === "dendrites")
    }
    "have the configured dispatcher" in {
      assert(system.dispatchers.hasDispatcher("dendrites.blocking-dispatcher") === true)
    }
  }
}