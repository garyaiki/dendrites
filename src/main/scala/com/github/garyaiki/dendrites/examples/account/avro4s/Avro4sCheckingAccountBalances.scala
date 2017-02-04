package com.github.garyaiki.dendrites.examples.account.avro4s

import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream, AvroSchema, FromRecord, SchemaFor, ToSchema, ToRecord}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import org.apache.avro.Schema
import com.github.garyaiki.dendrites.avro4s.Ops

/** Functions for Avro serialization and deserialization
  *
  * accountTypes: Set[AccountType] doesn't map to Avro, so use GetCustomerStringAccountBalances with
  * accountTypes: Seq[String]
  *
  * @author Gary Struthers
  */
object Avro4sCheckingAccountBalances  extends Ops[CheckingAccountBalances] {
  implicit val schema = AvroSchema[CheckingAccountBalances]

  /*implicit object BalanceToSchema extends ToSchema[Balance] {
    override val schema: Schema = AvroSchema[Balance](Avro4sBalance.schemaFor)
  }*/
  implicit val schemaFor = SchemaFor[CheckingAccountBalances]

  implicit val fromRecord = FromRecord[CheckingAccountBalances]

  implicit val toRecord = ToRecord[CheckingAccountBalances]
  def toBytes(caseClass: CheckingAccountBalances): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[CheckingAccountBalances](baos)
    output.write(caseClass)
    output.close()
    baos.toByteArray
  }

  def toCaseClass(bytes: Array[Byte]): CheckingAccountBalances = {
    val in = new ByteArrayInputStream(bytes)
    val input = AvroInputStream.binary[CheckingAccountBalances](in)
    val result = input.iterator.toSeq
    result(0)
  }
}