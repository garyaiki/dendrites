package com.github.garyaiki.dendrites.examples.account.avro4s

import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream, AvroSchema, FromRecord, SchemaFor, ToRecord}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import org.apache.avro.Schema
import com.github.garyaiki.dendrites.avro4s.Avro4sOps

/** GetCustomerStringAccountBalances serialize/deserialize with Avro4s.
  *
  *
  * @author Gary Struthers
  */
object Avro4sGetCustomerStringAccountBalances extends Avro4sOps[GetCustomerStringAccountBalances] {
  implicit val schemaFor = SchemaFor[GetCustomerStringAccountBalances]
  implicit val toRecord = ToRecord[GetCustomerStringAccountBalances]
  implicit val fromRecord = FromRecord[GetCustomerStringAccountBalances]
  val schema: Schema = AvroSchema[GetCustomerStringAccountBalances]

  /** GetCustomerStringAccountBalances to Array[Byte]
    *
    * @param caseClass
    * @return Array[Byte]
    */
  def toBytes(caseClass: GetCustomerStringAccountBalances): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[GetCustomerStringAccountBalances](baos)
    output.write(caseClass)
    output.close()
    baos.toByteArray
  }

  /** Array[Byte] to GetCustomerStringAccountBalances
    *
    * @param bytes
    * @return GetCustomerStringAccountBalances
    */
  def toCaseClass(bytes: Array[Byte]): GetCustomerStringAccountBalances = {
    val in = new ByteArrayInputStream(bytes)
    val input = AvroInputStream.binary[GetCustomerStringAccountBalances](in)
    val result = input.iterator.toSeq
    result(0)
  }
}
