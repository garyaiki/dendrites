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
package com.github.garyaiki.dendrites.avro

import org.apache.avro.generic.{GenericDatumReader, GenericRecord}
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import scala.io.Source._
import com.github.garyaiki.dendrites.examples.account.GetAccountBalances

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

      bytes.length shouldBe 1
      bytes(0).toString() shouldBe "2" // zigzag encoding

      def record = byteArrayToGenericRecord(schema, bytes)
      val gab2 = genericRecordToGetAccountBalances(record)

      gab2 shouldBe gab
    }
  }
}
