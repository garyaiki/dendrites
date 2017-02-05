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
package com.github.garyaiki.dendrites.examples.account.avro

import org.apache.avro.generic.{ GenericDatumReader, GenericRecord }
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import com.github.garyaiki.dendrites.avro.{ byteArrayToGenericRecord, loadSchema }
import com.github.garyaiki.dendrites.examples.account.{ Checking, GetCustomerAccountBalances, MoneyMarket }
import AvroGetCustomerAccountBalances.{ fromRecord, schemaFor, toBytes, toRecord }
/**
  * @author Gary Struthers
  *
  */
class AvroGetCustomerAccountBalancesSpec extends WordSpecLike {
  val schema = schemaFor(Some("/avro/"), "getCustomerStringAccountBalances.avsc")

  "An AvroGetCustomerAccountBalances" should {
    "serialize a GetCustomerAccountBalances case class from a schema and deserialize it back" in {

      val gab = GetCustomerAccountBalances(1L, Set(Checking))
      val bytes = toBytes(schema, gab)

      bytes.length shouldBe 12
      bytes(0).toString shouldBe "2" // zigzag encoding

      def record = byteArrayToGenericRecord(schema, bytes)

      System.out.println(s"deSer record:${record.toString}")

      val gab2 = fromRecord(record)

      gab2 shouldBe gab
    }
    "serialize another GetCustomerAccountBalances case class from a schema and deserialize it back" in {
      val gab = GetCustomerAccountBalances(1L, Set(Checking, MoneyMarket))
      val bytes = toBytes(schema, gab)

      bytes.length shouldBe 24
      bytes(0).toString shouldBe "2" // zigzag encoding

      def record = byteArrayToGenericRecord(schema, bytes)
      val gab2 = fromRecord(record)

      gab2 shouldBe gab
    }
  }

}
