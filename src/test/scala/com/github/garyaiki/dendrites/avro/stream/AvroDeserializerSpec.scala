/** Copyright 2016 Gary Struthers

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
package com.github.garyaiki.dendrites.avro.stream

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericDatumReader, GenericRecord}
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import com.github.garyaiki.dendrites.examples.account.GetAccountBalances
import com.github.garyaiki.dendrites.examples.account.avro.AvroGetAccountBalances

/**
  *
  * @author Gary Struthers
  *
  */
class AvroDeserializerSpec extends WordSpecLike {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)
  val avroOps = AvroGetAccountBalances
  val schema: Schema = avroOps.schemaFor(Some("/avro/"), "getAccountBalances.avsc")

  "An AvroDeserializer" should {
    "deserialize a case class from a schema and deserialize it back" in {

      val serializer = new AvroSerializer(schema, avroOps.toBytes)
      val deserializer = new AvroDeserializer(schema, avroOps.fromRecord)
      val (pub, sub) = TestSource.probe[GetAccountBalances]
        .via(serializer)
        .via(deserializer)
        .toMat(TestSink.probe[GetAccountBalances])(Keep.both)
        .run
      sub.request(1)
      val gab = GetAccountBalances(1L)
      pub.sendNext(gab)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      response shouldBe gab
    }
  }
}
