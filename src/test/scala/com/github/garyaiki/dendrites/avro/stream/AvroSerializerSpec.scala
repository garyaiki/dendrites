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
package org.gs.avro.stream

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import org.apache.avro.generic.{GenericDatumReader, GenericRecord}
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import scala.io.Source._
import org.gs.avro.{byteArrayToGenericRecord, ccToByteArray, loadSchema}
import org.gs.examples.account.GetAccountBalances

/**
  *
  * @author Gary Struthers
  *
  */
class AvroSerializerSpec extends WordSpecLike {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  "An AvroSerializer" should {
    "serialize a case class from a schema and deserialize it back" in {

      val serializer = new AvroSerializer("getAccountBalances.avsc", ccToByteArray)
      val (pub, sub) = TestSource.probe[GetAccountBalances]
        .via(serializer)
        .toMat(TestSink.probe[Array[Byte]])(Keep.both)
        .run
      sub.request(1)
      val gab = GetAccountBalances(1L)
      pub.sendNext(gab)
      val response = sub.expectNext()
      pub.sendComplete()
      sub.expectComplete()

      response.length shouldBe 1
      response(0).toString() shouldBe "2" // zigzag encoding

      val schema = loadSchema("getAccountBalances.avsc")

      def record = byteArrayToGenericRecord(schema, response)
      val obj = record.get("id")
      val l:Long = obj.asInstanceOf[Number].longValue
      val gab2 = GetAccountBalances(l)

      gab2 shouldBe gab
    }
  }
}
