package org.gs.examples.account.kafka

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.common.TopicPartition
import org.scalatest.WordSpecLike
import org.scalatest._
import org.scalatest.Matchers._
import scala.concurrent.Await
import scala.concurrent.duration._
import org.gs.examples.account.GetAccountBalances
import org.gs.examples.account.kafka.fixtures.AccountConsumerFixture
import org.gs.kafka.stream.KafkaSource

class AccountSourceSpec extends WordSpecLike with AccountConsumerFixture {
  implicit val system = ActorSystem("dendrites")
  implicit val logger = Logging(system, getClass)
  implicit val materializer = ActorMaterializer()
  "An AccountConsumer" should {
    "list Kafka partitions for topic" in {
      val partitionInfos = consumer.partitionsFor(accountConsumerFacade.topic)
      val pInfo = partitionInfos.get(0)
      assert(pInfo.partition === 0)
      assert(pInfo.topic === "account-topic")
      val leadNode = pInfo.leader()
      assert(leadNode.host === "localhost")
      assert(leadNode.id === 0)
      assert(leadNode.port === 2181)
      val topicPartition = new TopicPartition(pInfo.topic, pInfo.partition)
      val position = consumer.position(topicPartition)
      assert(position === 5)
    }
  }

  "An AccountKafkaSource" should {
    "poll a long from Kafka" in {
      val sourceGraph = new KafkaSource[String, Array[Byte]](accountConsumerFacade)
      val sourceUnderTest = Source.fromGraph(sourceGraph)
      val future = sourceUnderTest.grouped(5).runWith(Sink.head)
      val result = Await.result(future, 1000.millis)
      assert(result == Seq(GetAccountBalances(1),
          GetAccountBalances(2),
          GetAccountBalances(3),
          GetAccountBalances(4),
          GetAccountBalances(5)))
    }
  }
}
