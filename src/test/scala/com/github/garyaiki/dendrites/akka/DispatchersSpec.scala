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
package com.github.garyaiki.dendrites.akka

import akka.actor.ActorSystem
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._

class DispatchersSpec extends WordSpecLike {
  implicit val system = ActorSystem("dendrites")
  "An ActorSystem" should {
    "have the configured system name" in {
      system.name shouldBe "dendrites"
    }
    "have the configured dispatcher" in {
      system.dispatchers.hasDispatcher("dendrites.blocking-dispatcher") shouldBe true
    }
  }
}
