/**
  */
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
import org.gs.examples.account.avro._

/**
  *
  * @author Gary Struthers
  *
  */
class AvroDeserializerSpec extends WordSpecLike {
  implicit val system = ActorSystem("dendrites")
  implicit val materializer = ActorMaterializer()
  implicit val logger = Logging(system, getClass)

  "An AvroDeserializer" should {
    "deserialize a case class from a schema and deserialize it back" in {

      val serializer = new AvroSerializer("getAccountBalances.avsc", ccToByteArray)
      val deserializer = new AvroDeserializer("getAccountBalances.avsc",
              genericRecordToGetAccountBalances)
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

      assert(response === gab)
    }
  }
}
