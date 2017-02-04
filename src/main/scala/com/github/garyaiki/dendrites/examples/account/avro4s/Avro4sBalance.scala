package com.github.garyaiki.dendrites.examples.account.avro4s

import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream, AvroSchema, FromRecord, SchemaFor, ToRecord}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import org.apache.avro.Schema
import com.github.garyaiki.dendrites.avro4s.Ops
import com.github.garyaiki.dendrites.examples.account.GetCustomerStringAccountBalances

object Avro4sBalance extends Ops[Balance] {
  implicit val schemaFor = SchemaFor[Balance]
  val schema: Schema = AvroSchema[Balance]
  implicit val fromRecord = FromRecord[Balance]
  implicit val toRecord = ToRecord[Balance]
  def toBytes(caseClass: Balance): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[Balance](baos)
    output.write(caseClass)
    output.close()
    baos.toByteArray
  }

  def toCaseClass(bytes: Array[Byte]): Balance = {
    val in = new ByteArrayInputStream(bytes)
    val input = AvroInputStream.binary[Balance](in)
    val result = input.iterator.toSeq
    result(0)
  }
}