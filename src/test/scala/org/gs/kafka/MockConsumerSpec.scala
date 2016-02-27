package org.gs.kafka

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords}
import org.apache.kafka.common.TopicPartition
import org.scalatest.WordSpecLike
import org.scalatest._
import org.scalatest.Matchers._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

/** Test a Kafka MockConsumer in a Source */ 
class MockConsumerSpec extends WordSpecLike {
  implicit val system = ActorSystem("dendrites")
  implicit val logger = Logging(system, getClass)
  val mockConsumerFacade = MockConsumerFacade

        
  "A MockConsumer" should {
    "have valid properties" in {
      val mc = mockConsumerFacade.createConsumer()
      val subscription = mc.subscription()
      assert(subscription.contains("akkaKafka"))
      val comsumerRecords = mc.poll(mockConsumerFacade.timeout)
      assert(comsumerRecords.count === 7)
      mc.close()
    }
  }
}
