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
package com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.avro4s

import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream, AvroSchema, FromRecord, SchemaFor, ToRecord}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import com.github.garyaiki.dendrites.avro4s.Avro4sOps
import com.sksamuel.avro4s.SchemaFor
import com.github.garyaiki.dendrites.examples.cqrs.shoppingcart.cmd.ShoppingCartCmd

/** ShoppingCartCmd serialize/deserialize with Avro4s.
  *
  *
  * @author Gary Struthers
  */
object Avro4sShoppingCartCmd  extends Avro4sOps[ShoppingCartCmd] {
  implicit val schemaFor = SchemaFor[ShoppingCartCmd]
  implicit val toRecord = ToRecord[ShoppingCartCmd]
  implicit val fromRecord = FromRecord[ShoppingCartCmd]
  val schema = AvroSchema[ShoppingCartCmd]

  /** ShoppingCartCmd to Array[Byte]
    *
    * @param caseClass
    * @return Array[Byte]
    */
  def toBytes(caseClass: ShoppingCartCmd): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[ShoppingCartCmd](baos)
    output.write(caseClass)
    output.close()
    baos.toByteArray
  }

  /** Array[Byte] to ShoppingCartCmd
    *
    * @param bytes
    * @return ShoppingCartCmd
    */
  def toCaseClass(bytes: Array[Byte]): ShoppingCartCmd = {
    val in = new ByteArrayInputStream(bytes)
    val input = AvroInputStream.binary[ShoppingCartCmd](in)
    val result = input.iterator.toSeq
    result(0)
  }
}
