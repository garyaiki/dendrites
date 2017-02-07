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
package com.github.garyaiki.dendrites.examples.account.avro4s

import com.sksamuel.avro4s.AvroSchema
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import com.github.garyaiki.dendrites.avro.loadSchema
import com.github.garyaiki.dendrites.examples.account.{Checking, MoneyMarket, GetCustomerAccountBalances, Savings}
import Avro4sGetCustomerAccountBalances.{toBytes, toCaseClass}
import Avro4sGetCustomerStringAccountBalances.schemaFor

/** Avro4sGetCustomerStringAccountBalances schema
  * {"type":"record","name":"GetCustomerAccountBalances","namespace":"com.github.garyaiki.dendrites.examples.account",
  * "fields":[{"name":"id","type":"long"},{"name":"accountTypes","type":
  * {"type":"array","items":"string"}}]}
  * @author Gary Struthers
  *
  */
class Avro4sGetCustomerAccountBalancesSpec extends WordSpecLike {

  "An Avro4sGetCustomerAccountBalances" should {

    val schema = loadSchema("GetCustomerStringAccountBalances.avsc", "/avro/")

    "produce a schema matching file schema" in {
      AvroSchema[GetCustomerStringAccountBalances] shouldBe schema
    }
    "serialize a GetCustomerAccountBalances Checking case class and deserialize it back with Avro4s" in {
      val gab = GetCustomerAccountBalances(1L, Set(Checking))

      val bytes = toBytes(gab)
      bytes.length shouldBe 12
      bytes(0).toString() shouldBe "2" // zigzag encoding
      val gab2 = toCaseClass(bytes)
      gab2 shouldBe gab
    }
    "serialize a GetCustomerAccountBalances MoneyMarket case class and deserialize it back with Avro4s" in {
      val gab = GetCustomerAccountBalances(1L, Set(MoneyMarket))

      val bytes = toBytes(gab)
      val gab2 = toCaseClass(bytes)
      gab2 shouldBe gab
    }
    "serialize a GetCustomerAccountBalances MoneyMarket, Savings case class and deserialize it back with Avro4s" in {
      val gab = GetCustomerAccountBalances(1L, Set(MoneyMarket, Savings))

      val bytes = toBytes(gab)
      val gab2 = toCaseClass(bytes)
      gab2 shouldBe gab
    }
  }

}
