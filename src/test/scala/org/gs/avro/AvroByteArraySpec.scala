
package org.gs.avro

import org.apache.avro.generic.{GenericDatumReader, GenericRecord}
import org.scalatest.WordSpecLike
import scala.io.Source._
import org.gs.examples.account.GetAccountBalances

/**
  *
  * @author Gary Struthers
  *
  */
class AvroByteArraySpec extends WordSpecLike {

  def genericRecordToGetAccountBalances(record: GenericRecord): GetAccountBalances = {
      val obj = record.get("id")
      val l:Long = obj.asInstanceOf[Number].longValue
      GetAccountBalances(l)
  }
  "An AvroByteArray" should {
    "serialize a case class from a schema and deserialize it back" in {
      val schema = loadSchema("getAccountBalances.avsc")
      val gab = GetAccountBalances(1L)
      val bytes = ccToByteArray(schema, gab)

      assert(bytes.length === 1)
      assert(bytes(0).toString() === "2") // zigzag encoding

      def record = byteArrayToGenericRecord(schema, bytes)
      val gab2 = genericRecordToGetAccountBalances(record)

      assert(gab2 === gab)
    }
  }
}
