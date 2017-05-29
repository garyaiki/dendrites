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
package com.github.garyaiki.dendrites.kafka

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._

/** Test a Kafka MockConsumer in a Source */
class MockConsumerSpec extends WordSpecLike {
  implicit val system = ActorSystem("dendrites")
  implicit val logger = Logging(system, getClass)
  val mockConsumerFacade = MockConsumerConfig

  "A MockConsumer" should {
    "have valid properties" in {
      val mc = mockConsumerFacade.createAndSubscribe()
      val subscription = mc.subscription()
      subscription should contain("akkaKafka")

      val comsumerRecords = mc.poll(mockConsumerFacade.timeout)

      comsumerRecords.count should equal(7)
      mc.close()
    }
  }
}
