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
package com.github.garyaiki.dendrites

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import org.apache.avro.generic.GenericRecord
import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream, FromRecord, FromValue, RecordFormat, SchemaFor, ToRecord}

/** Extend Avro4sOps to serialize/deserialize case class
  * {{{
  * object Avro4sShoppingCartCmd  extends Avro4sOps[ShoppingCartCmd] {
  *   implicit val schemaFor = SchemaFor[ShoppingCartCmd]
  *   implicit val toRecord = ToRecord[ShoppingCartCmd]
  *   implicit val fromRecord = FromRecord[ShoppingCartCmd]
  * }}}
  * implement case class serializer
  * {{{
  *   def toBytes(caseClass: ShoppingCartCmd): Array[Byte] = {
  *     val baos = new ByteArrayOutputStream()
  *     val output = AvroOutputStream.binary[ShoppingCartCmd](baos)
  *     output.write(caseClass)
  *     output.close()
  *     baos.toByteArray
  *   }
  * }}}
  * implement deserializer
  * {{{
  *   def toCaseClass(bytes: Array[Byte]): ShoppingCartCmd = {
  *     val in = new ByteArrayInputStream(bytes)
  *     val input = AvroInputStream.binary[ShoppingCartCmd](in)
  *     val result = input.iterator.toSeq
  *     result(0)
  *   }
  * }}}
  */
package object avro4s  {

  /** RecordFormat contains toRecord, fromRecord
    *
    * @param toRec case class to GenericRecord
    * @param fromRec GenericRecord to case class
    * @return
    * @see [[https://github.com/sksamuel/avro4s avro4s]]
    */
  def createRecordFormat[A](implicit toRec: ToRecord[A], fromRec: FromRecord[A]): RecordFormat[A] = RecordFormat[A]

  /** case class to GenericRecord
    *
    * @param cc case class
    * @param format RecordFormat for case class
    * @return GenericRecord
    */
  def caseClassToGenericRecord[A](cc: A)(implicit format: RecordFormat[A]): GenericRecord = format.to(cc)

  /** GenericRecord to case class
    *
    * @param record
    * @param format
    * @return
    */
  def genericRecordToCaseClass[A](record: GenericRecord)(implicit format: RecordFormat[A]): A = format.from(record)

  /** case class to Array[Byte
    *
    * @param cc case class
    * @param schema4 SchemaFor case class
    * @param toRec ToRecord for case class
    * @return Array[Byte]
    */
  def caseClassToBytes[A](cc: A)(implicit schema4: SchemaFor[A], toRec: ToRecord[A] ): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.data[A](baos)
    output.write(cc)
    output.close()
    baos.toByteArray()
  }

  /** Array[Byte] to case class
    *
    * @param bytes Array[Byte]
    * @param schema4 SchemaFor[A]
    * @param fromRec FromRecord[A]
    * @return case class
    */
  def bytesToCaseClass[A](bytes: Array[Byte])(implicit schema4: SchemaFor[A], fromRec: FromRecord[A]): A = {
    val input = AvroInputStream.data[A](bytes) // (bais)

    input.iterator.next() // fail if no first element
  }
}
