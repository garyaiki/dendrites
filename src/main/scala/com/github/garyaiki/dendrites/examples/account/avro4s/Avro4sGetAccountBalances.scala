package com.github.garyaiki.dendrites.examples.account.avro4s

import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream, FromRecord, SchemaFor, ToRecord}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import com.github.garyaiki.dendrites.avro4s.Ops
import com.github.garyaiki.dendrites.examples.account.GetAccountBalances

object Avro4sGetAccountBalances extends Ops[GetAccountBalances] {
  implicit val schemaFor = SchemaFor[GetAccountBalances]
  implicit val fromRecord = FromRecord[GetAccountBalances]
  implicit val toRecord = ToRecord[GetAccountBalances]
  def toBytes(caseClass: GetAccountBalances): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[GetAccountBalances](baos)
    output.write(caseClass)
    output.close()
    baos.toByteArray
  }

  def toCaseClass(bytes: Array[Byte]): GetAccountBalances = {
    val in = new ByteArrayInputStream(bytes)
    val input = AvroInputStream.binary[GetAccountBalances](in)
    val result = input.iterator.toSeq
    result(0)
  }
}