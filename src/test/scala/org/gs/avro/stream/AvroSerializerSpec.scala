
package org.gs.avro.stream

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import org.apache.avro.generic.{GenericDatumReader, GenericRecord}
import org.scalatest.WordSpecLike
import scala.io.Source._
import org.gs._
import org.gs.avro._
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
      assert(response.length === 1)
      assert(response(0).toString() === "2") // zigzag encoding
      val schema = loadSchema("getAccountBalances.avsc")

      def record = byteArrayToGenericRecord(schema, response)
      val obj = record.get("id")
      val l:Long = obj.asInstanceOf[Number].longValue
      val gab2 = GetAccountBalances(l)
      assert(gab2 === gab)
    }
  }
}
