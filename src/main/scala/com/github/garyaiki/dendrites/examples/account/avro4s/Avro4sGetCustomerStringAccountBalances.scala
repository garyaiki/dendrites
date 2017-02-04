package com.github.garyaiki.dendrites.examples.account.avro4s

import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream, FromRecord, SchemaFor, ToRecord}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import com.github.garyaiki.dendrites.avro4s.Ops
import com.github.garyaiki.dendrites.examples.account.GetCustomerStringAccountBalances

/** Functions for Avro serialization and deserialization
  *
  * accountTypes: Set[AccountType] doesn't map to Avro, so use GetCustomerStringAccountBalances with
  * accountTypes: Seq[String]
  *
  * @author Gary Struthers
  */
object Avro4sGetCustomerStringAccountBalances extends Ops[GetCustomerStringAccountBalances] {
  /** case class only for ser/deSer */
  implicit val schemaFor = SchemaFor[GetCustomerStringAccountBalances]
  implicit val fromRecord = FromRecord[GetCustomerStringAccountBalances]
  implicit val toRecord = ToRecord[GetCustomerStringAccountBalances]
  def toBytes(caseClass: GetCustomerStringAccountBalances): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[GetCustomerStringAccountBalances](baos)
    output.write(caseClass)
    output.close()
    baos.toByteArray
  }

  def toCaseClass(bytes: Array[Byte]): GetCustomerStringAccountBalances = {
    val in = new ByteArrayInputStream(bytes)
    val input = AvroInputStream.binary[GetCustomerStringAccountBalances](in)
    val result = input.iterator.toSeq
    result(0)
  }
}