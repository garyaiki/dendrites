package org.gs.kafka

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

      assert(subscription.contains("akkaKafka"))
      val comsumerRecords = mc.poll(mockConsumerFacade.timeout)

      assert(comsumerRecords.count === 7)
      mc.close()
    }
  }
}
