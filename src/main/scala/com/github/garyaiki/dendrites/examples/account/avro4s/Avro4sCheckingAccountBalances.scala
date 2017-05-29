/**
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
package com.github.garyaiki.dendrites.examples.account.avro4s

import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream, AvroSchema, FromRecord, SchemaFor, ToRecord, ToSchema}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import org.apache.avro.Schema
import com.github.garyaiki.dendrites.avro4s.Avro4sOps

/** CheckingAccountBalances serialize/deserialize with Avro4s.
  *
  *
  * @author Gary Struthers
  */
object Avro4sCheckingAccountBalances  extends Avro4sOps[CheckingAccountBalances] {
  implicit val schemaFor = SchemaFor[CheckingAccountBalances]
  implicit val toRecord = ToRecord[CheckingAccountBalances]
  implicit val fromRecord = FromRecord[CheckingAccountBalances] // Scala-ide shows error, No error in sbt, maven
  val schema = AvroSchema[CheckingAccountBalances]

  /** CheckingAccountBalances to Array[Byte]
    *
    * @param caseClass
    * @return Array[Byte]
    */
  def toBytes(caseClass: CheckingAccountBalances): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[CheckingAccountBalances](baos)
    output.write(caseClass)
    output.close()
    baos.toByteArray
  }

  /** Array[Byte] to CheckingAccountBalances
    *
    * @param bytes
    * @return CheckingAccountBalances
    */
  def toCaseClass(bytes: Array[Byte]): CheckingAccountBalances = {
    val in = new ByteArrayInputStream(bytes)
    val input = AvroInputStream.binary[CheckingAccountBalances](in)
    val result = input.iterator.toSeq
    result(0)
  }
}
