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
package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.{ActorAttributes, ActorMaterializer}
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import com.typesafe.config.ConfigFactory
import java.util.UUID
import org.apache.avro.Schema
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatest.time.SpanSugar._
import scala.concurrent.ExecutionContext
import scala.collection.immutable.{Iterable, Seq}
import com.github.garyaiki.dendrites.avro4s.stream.Avro4sSerializer
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.ShoppingCartCmd
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.avro4s.Avro4sShoppingCartCmd
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.avro4s.Avro4sShoppingCartCmd.toCaseClass
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.kafka.{ShoppingCartCmdConsumer,
  ShoppingCartCmdProducer}
import com.github.garyaiki.dendrites.kafka.stream.avro4s.ConsumerRecordDeserializer
import com.github.garyaiki.dendrites.kafka.stream.{ConsumerRecordsToQueue, KafkaSink, KafkaSource}
import com.github.garyaiki.dendrites.kafka.stream.extractRecords

/** Test integration of Kafka with Akka Streams. There are 2 multi-stage flows. The first stream
  * serializes case classes to Avro byteArrays then writes them to Kafka. The second stream reads
  * those byteArrays from Kafka and deserializes them back to case classes.
  *
  *
  * @author Gary Struthers
  *
  */
class ShoppingCartCmdSpec extends WordSpecLike with Matchers with BeforeAndAfterAll {
  implicit val system = ActorSystem("dendrites")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val avro4sOps = Avro4sShoppingCartCmd
  val ap = ShoppingCartCmdProducer
  val consumerConfig = ShoppingCartCmdConsumer
  val cartId = UUID.randomUUID
  val firstOwner = UUID.randomUUID
  val secondOwner = UUID.randomUUID
  val firstItem = UUID.randomUUID
  val secondItem = UUID.randomUUID
  val getCmds = Seq(ShoppingCartCmd("SetOwner", cartId, firstOwner, None),
    ShoppingCartCmd("AddItem", cartId, firstItem, Some(1)),
    ShoppingCartCmd("AddItem", cartId, secondItem, Some(1)),
    ShoppingCartCmd("AddItem", cartId, firstItem, Some(1)),
    ShoppingCartCmd("AddItem", cartId, secondItem, Some(1)),
    ShoppingCartCmd("AddItem", cartId, secondItem, Some(1)),
    ShoppingCartCmd("SetOwner", cartId, secondOwner, None),
    ShoppingCartCmd("RemoveItem", cartId, firstItem, Some(1)),
    ShoppingCartCmd("RemoveItem", cartId, secondItem, Some(1)),
    ShoppingCartCmd("RemoveItem", cartId, secondItem, Some(1)))

  "A KafkaStream" should {
    "write then recreate case classes using ConsumerRecordsToQueue" in {
      val iter = Iterable(getCmds.toSeq:_*)
      val serializer = new Avro4sSerializer(avro4sOps.toBytes)
      val sink = KafkaSink[String, Array[Byte]](ap)
      val source = Source[ShoppingCartCmd](iter)
      source.via(serializer).runWith(sink)

      val dispatcher = ActorAttributes.dispatcher("dendrites.blocking-dispatcher")

      val kafkaSource = KafkaSource[String, Array[Byte]](consumerConfig).withAttributes(dispatcher)
      val consumerRecordQueue = new ConsumerRecordsToQueue[String, Array[Byte]](extractRecords)
      val deserializer = new ConsumerRecordDeserializer[String, ShoppingCartCmd](toCaseClass)
      val results = kafkaSource
        .via(consumerRecordQueue)
        .via(deserializer)
        .runWith(TestSink.probe[(String, ShoppingCartCmd)])
        .request(10)
        .expectNextN(10)
      results forall(kv => getCmds.contains(kv._2))
    }
  }

  override def afterAll() {
    ap.producer.flush()
    val config = ConfigFactory.load()
    val closeTimeout = config.getLong("dendrites.kafka.close-timeout")
    Thread.sleep(closeTimeout)
    ap.producer.close(closeTimeout, scala.concurrent.duration.MILLISECONDS)
  }
}
