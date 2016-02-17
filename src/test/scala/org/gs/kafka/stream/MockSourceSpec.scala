package org.gs.kafka.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.{ActorMaterializer, Graph, SourceShape}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords}
import org.apache.kafka.common.TopicPartition
import org.scalatest.WordSpecLike
import org.scalatest._
import org.scalatest.Matchers._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import org.gs.examples.account.kafka.fixtures.MockAccountConsumerFixture

/** Test a Kafka MockConsumer in a Source */ 
class MockSourceSpec extends WordSpecLike with MockAccountConsumerFixture {
  implicit val system = ActorSystem("dendrites")
  implicit val logger = Logging(system, getClass)
  implicit val materializer = ActorMaterializer()

  val source = KafkaSource[String, String](mockConsumerFacade)
        
  "An MockKafkaSource" should {
    "poll ConsumeRecords from Kafka" in {
      val future = source.grouped(1).runWith(Sink.head)
      val result = Await.result(future, 1000.millis)
      var crs: ConsumerRecords[String, String] = null
      result match {
        case x: Vector[ConsumerRecords[String, String]] => crs = x(0)
      }
      assert(crs.count() === 7)
    }
  }
}
