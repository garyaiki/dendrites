package org.gs.examples.account.kafka

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import org.scalatest.WordSpecLike
import org.scalatest._
import org.scalatest.Matchers._
import scala.concurrent.Await
import scala.concurrent.duration._
import org.gs.examples.account.kafka.fixtures.AccountProducerFixture
import org.gs.kafka.stream.KafkaSink

class AccountSinkSpec extends WordSpecLike with AccountProducerFixture {
  implicit val system = ActorSystem("dendrites")
  implicit val logger = Logging(system, getClass)
  implicit val materializer = ActorMaterializer()
  "An AccountKafkaSink" should {
    "send a long to Kafka" in {
      val sink = KafkaSink[String, Array[Byte]](ap)
      val sinkUnderTest = Flow[Long].map(_ * 2).toMat(Sink.fold(0L)(_ + _))(Keep.right)
      val future = Source(1L to 4L).runWith(sinkUnderTest)
      val result = Await.result(future, 1000.millis)
      assert(result == 20)
    }
  }
}
