/** Copyright 2016 Gary Struthers
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.github.garyaiki.dendrites.avro4s.stream

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import com.github.garyaiki.dendrites.examples.account.avro4s.{ Balance, CheckingAccountOptBalances }
import com.github.garyaiki.dendrites.examples.account.avro4s.Avro4sCheckingAccountOptBalances

/**
  * @author Gary Struthers
  *
  */
class Avro4sSerializerDeserializerSpec extends WordSpecLike {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val avro4sOps = Avro4sCheckingAccountOptBalances
  val balances = Seq(Balance(1L, 0.00), Balance(10L, 10.10), Balance(99L, 99.99))

  "An Avro4sSerializer/Avro4sDeserializer" should {
    "serialize a case class and deserialize it back" in {

      val serializer = new Avro4sSerializer(avro4sOps.toBytes)
      val deserializer = new Avro4sDeserializer[CheckingAccountOptBalances](avro4sOps.toCaseClass)
      val (pub, sub) = TestSource.probe[CheckingAccountOptBalances]
        .via(serializer)
        .via(deserializer)
        .toMat(TestSink.probe[CheckingAccountOptBalances])(Keep.both)
        .run
      sub.request(1)
      val gab = CheckingAccountOptBalances(Some(balances))
      pub.sendNext(gab)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      response shouldBe gab

    }
  }
}
