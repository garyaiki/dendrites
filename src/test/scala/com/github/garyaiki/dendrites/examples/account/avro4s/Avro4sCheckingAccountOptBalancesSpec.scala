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
import org.scalatest.WordSpecLike
import org.scalatest.Matchers._
import com.github.garyaiki.dendrites.avro.loadSchema
import Avro4sCheckingAccountOptBalances.{schemaFor, toBytes, toCaseClass}

/** @author Gary Struthers
  *
  */
class Avro4sCheckingAccountOptBalancesSpec extends WordSpecLike {

  val schema = loadSchema("checkingAccountOptBalances.avsc", "/avro/")
  "An Avro4sCheckingAccountOpyBalances" should {
    "produce a schema matching file schema" in {
      AvroSchema[CheckingAccountOptBalances] shouldBe schema
    }
    "serialize a CheckingAccountOptBalances case class and deserialize it back with Avro4s" in {
      val gab = CheckingAccountOptBalances(Some(List(Balance(1L, 0.0))))

      val bytes = toBytes(gab)

      val gab2 = toCaseClass(bytes)
      gab2 shouldBe gab
    }
  }
}
